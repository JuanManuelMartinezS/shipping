
package poo.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import poo.helpers.Utils;
import poo.model.Client;
import poo.model.Delivery;
import poo.model.DeliveryStatus;
import poo.model.Status;

public class DeliveryService implements Service<Delivery> {

  private Class<? extends Delivery> subclass;
  private final Service<Client> clients;
  private final String fileName;
  protected List<Delivery> list;

  public DeliveryService(Class<? extends Delivery> subclass, Service<Client> clients) throws Exception {
    this.subclass = subclass;
    this.clients = clients;
    fileName = Utils.PATH + subclass.getSimpleName() + ".json";

    if (Utils.fileExists(fileName)) {
      load();
    } else {
      list = new ArrayList<>();
    }
  }

  @Override
  public JSONObject add(String strJson) throws Exception {
    Delivery d = dataToAddOk(strJson);
    if (list.add(d)) {
      Utils.writeJSON(list, fileName);
    }
    return new JSONObject().put("message", "ok").put("data", d.toJSONObject());

  }

  @Override
  public JSONObject get(int index) {
    return list.get(index).toJSONObject();
  }

  @Override
  public JSONObject get(String numGuide) throws Exception {
    Delivery d = getItem(numGuide);
    if (d == null) {
      throw new NoSuchElementException(String.format("A shipment with tracking number %s was not found.", numGuide));
    }
    return d.toJSONObject();
  }

  @Override
  public Delivery getItem(String numGuide) throws Exception {
    Delivery e = subclass.getConstructor(String.class).newInstance(numGuide);
    int i = list.indexOf(e);
    return i > -1 ? list.get(i) : null;
  }

  @Override
  public JSONObject getAll() {
    try {
      JSONArray data = new JSONArray();
      if (Utils.fileExists(fileName)) {
        data = new JSONArray(Utils.readText(fileName));
      }
      return new JSONObject().put("message", "ok").put("data", data);
    } catch (IOException | JSONException e) {
      Utils.printStackTrace(e);
      return Utils.keyValueToJson("message", "No access to shipping data", "error", e.getMessage());
    }
  }

  @Override
  public final List<Delivery> load() throws Exception {
    list = new ArrayList<>();
    JSONArray jsonArr = new JSONArray(Utils.readText(fileName));
    for (int i = 0; i < jsonArr.length(); i++) {
      JSONObject jsonObj = jsonArr.getJSONObject(i);
      Delivery d = subclass.getConstructor(JSONObject.class).newInstance(jsonObj);
      list.add(d);
    }
    return list;
  }

  @Override
  public Delivery dataToAddOk(String strJson) throws Exception {
    JSONObject json = new JSONObject(strJson);
    // Si json no tiene el atributo nroGuia agregue uno aleatorio de 8 caracteres
    if (!json.has("numGuide")) {
      json.put("numGuide", Utils.getRandomKey(8));
    }

    if (!json.has("statuses")) {
      Status status = new Status(LocalDateTime.now().withNano(0), DeliveryStatus.RECEIVED);
      json.put("statuses", new JSONArray().put(status.toJSONObject()));
    }
    if (!json.has("isFragile")) {
      json.put("isFragile", false);
    }
    // intentar reemplazar en json el ID del
    // remitente y del destinatario por los JSONObject respectivos

    updateClient(json, "sender");
    updateClient(json, "addressee");

    Utils.stringOk("content", 3, json);
    Utils.doubleOk("value", 0, 10000000, json);
    Utils.doubleOk("weight", 0, 20000, json);

    // Mediante reflection asigne a una variable envio de tipo Envio, una instancia
    // de
    // tipo subclase creada a partir del objeto json.
    Delivery delivery = subclass.getConstructor(JSONObject.class).newInstance(json);

    if (json.getJSONObject("sender").equals(json.getJSONObject("addressee"))) {
      throw new IllegalArgumentException(
          String.format("An addressee other than the sender is expected: id = %s", json.getString("id")));
    }
    // Si list contiene un envio con número de guía igual al actual, lanzar una
    // ArrayStoreException
    for (Delivery d : list) {
      if (d.getNumGuide().equals(delivery.getNumGuide())) {
        throw new ArrayStoreException(
            String.format("A shipment with the guide number %s already exists.", delivery.getNumGuide()));
      }
    }
    return delivery;

  }

  @Override
  public JSONObject update(String numGuide, String strJson) throws Exception {
    JSONObject newData = new JSONObject(strJson);

    // Buscar y referenciar el envio que se debe actualizar
    Delivery delivery = subclass.cast(getItem(numGuide));

    if (delivery == null) {
      throw new NullPointerException("Shipment not found" + numGuide);
    }

    // Buscar la posicion del envio en la lista y actualizarlo
    int i = list.indexOf(delivery);
    try {
      delivery = getUpdated(newData, delivery);
    } catch (Exception e) {
      return new JSONObject().put("message", e.getMessage());
    }

    list.set(i, delivery);

    // Actualizar el archivo y retornar el reporte de la accion
    Utils.writeJSON(list, fileName);
    return new JSONObject().put("message", "ok").put("data", delivery.toJSONObject());

  }

  @Override
  public Delivery getUpdated(JSONObject newData, Delivery current) throws Exception {
    // Asignar en status el ultimo estado de los estados de current
    DeliveryStatus status = current.getStatuses().getLast().getDeliveryStatus();
    // Variable local
    JSONObject updated = current.toJSONObject();
    // Si el estado no es devuelto, en preparación, indefinido o RECEIVED lanzar una
    // IllegalStateException
    if (!"RETURNED|IN_PREPARATION|UNDEFINED|RECEIVED".contains(status.toString()) && !newData.has("statuses")) {
      throw new IllegalStateException(String.format("A shipment with status %s cannot be changed.", status.toString().toLowerCase()));
    }
    // Si newData tiene estados hacer las verificaciones, no se pueden actualizar
    // caracteristicas del envio despues de tener estados
    if (newData.has("statuses")) {
      // verificaciones
      if (validateStates(newData, current)) {
        JSONArray listStatuses = updated.getJSONArray("statuses");
        // Obtener el array de estados en la nueva informacion
        JSONArray statuses = newData.getJSONArray("statuses");
        // Obtener el primer estado que viene en la actualizacion
        JSONObject firstStatus = statuses.getJSONObject(0);
        // Insertar el estado nuevo al final de la lista de estados actualizada
        listStatuses.put(firstStatus);
        updated.put("statuses", listStatuses);

      }
    } // Si no tiene estados actualizar las caracteristicas del envio
    else {
      // Intentar cambiar el ID del remitente y del destinatario por los objetos
      // respectivos
      if (newData.has("sender")) {
        updateClient(updated, "sender", newData);
      }
      if (newData.has("addressee")) {
        updateClient(updated, "addressee", newData);
      }
      /*
       * Verificar si el objeto newData tiene los atributos peso, fragil, contenido,
       * valorDeclarado, certificado, ancho, alto y/o largo y de ser así, actualizar
       * los respectivos valores en updated, no sin antes validar que tengan datos
       * correctos.
       */
      if (newData.has("weight")) {
        updated.put("weight", Utils.doubleOk("weight", 0, 1600, newData));
      }
      if (newData.has("isFragile")) {
        updated.put("isFragile", newData.getBoolean("isFragile"));
      }
      if (newData.has("content")) {
        updated.put("content", Utils.stringOk("content", 3, newData));
      }
      if (newData.has("value")) {
        updated.put("value", Utils.doubleOk("value", 0, 10000000, newData));
      }
      if (newData.has("isCertified")) {
        updated.put("isCertified", newData.getBoolean("isCertified"));
      }
      if (newData.has("width")) {
        updated.put("width", Utils.doubleOk("width", 0.1, 2.44, newData));
      }
      if (newData.has("height")) {
        updated.put("height", Utils.doubleOk("height", 0.1, 2.59, newData));
      }
      if (newData.has("length")) {
        updated.put("length", Utils.doubleOk("length", 0.1, 12.19, newData));
      }

    }
    // Creo una nueva instancia de tipo subclase a partir del objeto updated
    Delivery d = subclass.getConstructor(JSONObject.class).newInstance(updated);
    return d;
  }

  /**
   * Establecer el orden logico de los estados, analizar todas las posibilidades
   * que pueden pasar,comprobar la cantidad de veces que se puede entregar un
   * envio, en caso de alguna irregularidad lanzar una excepcion
   */
  static public boolean validateStates(JSONObject newData, Delivery current) throws Exception {
    boolean verificado = true;
    // Obtener el estado actual del envio
    String ultimoEstado = new Status(current.getStatuses().getLast()).toString();
    // Obtener el array de estados en la nueva informacion
    JSONArray estados = newData.getJSONArray("statuses");
    // Obtener el primer estado que viene en la actualizacion
    JSONObject primNuevoEstado = estados.getJSONObject(0);
    // convertir el ultimo estado de JSONObject a Estado
    String nuevoEstado = new Status(primNuevoEstado).toString();

    // Verificar si el envio tiene mas de 2 intentos de entrega (reenvio)
    ArrayList<Status> listaEstados = new ArrayList<>();
    listaEstados = current.getStatuses();
    int contador = 0;

    for (Status estado : listaEstados) {
      if (estado.toString().equals("RESHIPPED")) {
        contador++;
      }
    }
    //Para hacer dos intentos de entrega, reenviado y por ultimo enviado
    if (nuevoEstado.equals("RESHIPPED") && contador >= 1) {
      throw new Exception(String.format(
          "A shipment cannot have more than 2 delivery attempts, the sender must pick it up at the office."));
    }

    /*
     * "RECEIVED" [0]
     * "IN_PREPARATION" [1]
     * "SENT" [2]
     * "ON_THE_WAY" [3]
     * "RESHIPPED" [4]
     * "DELIVERED" [5]
     * "RETURNED" [6]
     * "AVAILABLE_IN_OFICCE" [7]
     * "LOST" [8]
     */
    String[] orden = { "RECEIVED", "IN_PREPARATION", "SENT", "ON_THE_WAY", "RESHIPPED", "DELIVERED", "RETURNED",
        "AVAILABLE_IN_OFFICE", "LOST" };

    // Verificar si el envío fue devuelto
    if (ultimoEstado.equals(orden[6])) {
      throw new Exception(String.format("After returned, a shipment cannot be modified."));
    }

    // Verificar si el envio fue DELIVERED y el nuevo estado no es devuelto
    if (ultimoEstado.equals(orden[5]) && !nuevoEstado.equals(orden[6])) {
      throw new Exception(String.format("After delivered, a shipment can only be returned to"));
    }

    // Verificar si el envio fue RESHIPPED...
    if (ultimoEstado.equals(orden[4])
        && (!"DELIVERED|AVAILABLE_IN_OFFICE|LOST|RESHIPPED".contains(nuevoEstado))) {
      throw new Exception(String.format(
          "After reshipped, a shipment can only be delivered, available in office or lost."));
    }

    // Verificar si el envio está AVAILABLE en la oficina...
    if (ultimoEstado.equals(orden[7]) && (!"DELIVERED|LOST".contains(nuevoEstado))) {
      throw new Exception(
          String.format("After being available in the office, a shipment can only be delivered or lost"));
    }

    // Verificar si el envío está en camino...
    if (ultimoEstado.equals(orden[3]) && (!"DELIVERED|LOST|RESHIPPED".contains(nuevoEstado))) {
      throw new Exception(
          String.format("After being on the way, a shipment can only be delivered, lost, reshipped"));
    }

    // Verificar el orden del proceso hasta SENT
    if (ultimoEstado.equals(orden[0]) && (!"IN_PREPARATION|LOST".contains(nuevoEstado))) {
      throw new Exception(
          String.format("After received, a shipment can only be in preparation or lost."));
    }
    if (ultimoEstado.equals(orden[1]) && (!"SENT|LOST".contains(nuevoEstado))) {
      throw new Exception(
          String.format("After being in preparation, a shipment can only be sent or lost."));
    }
    if (ultimoEstado.equals(orden[2]) && (!"ON_THE_WAY|LOST".contains(nuevoEstado))) {
      throw new Exception(String.format("After being sent, a shipment can only be on the way or lost."));
    }
    return verificado;
  }

  private void updateClient(JSONObject json, String typeClient) throws Exception {
    String idClient = json.getString(typeClient);

    JSONObject client = clients.get(idClient);

    if (client == null) {
      throw new IllegalArgumentException(String.format("Error when determining client %s of shipment", idClient));
    }
    json.put(typeClient, client);
  }

  private void updateClient(JSONObject json, String typeClient, JSONObject newData) throws Exception {
    String idClient = newData.getString(typeClient);

    JSONObject client = clients.get(idClient);

    if (client == null) {
      throw new IllegalArgumentException(String.format("Error when determining client %s of shipment", idClient));
    }
    json.put(typeClient, client);
  }

  @Override
  public JSONObject size() {
    return new JSONObject().put("size", list.size()).put("message", "ok");
  }

  @Override
  public JSONObject remove(String numGuide) throws Exception {
    JSONObject delivery = get(numGuide);
    if (get(numGuide) == null) {
      throw new NoSuchElementException(String.format("No merchandises found with ID %s", numGuide));
    }
    Delivery c = subclass.getConstructor(JSONObject.class).newInstance(delivery);
    // Eliminar de list el envio c
    list.remove(c);
    // Actualizar el archivo de subclass
    Utils.writeJSON(list, fileName);
    // retorne una instancia de JSONObject con los atributos:
    return new JSONObject().put("message", "ok").put("data", delivery);
  }

  @Override
  public JSONObject removeStatus(String numGuide) throws Exception {
    // Obtener el jsonObject a partir del numero de guia
    JSONObject delivery = get(numGuide);
    if (get(numGuide) == null) {
      throw new NoSuchElementException(String.format("A shipment with ID %s was not found.", numGuide));
    }
    // Crear la instancia de envio a partir del Json obtenido
    Delivery c = subclass.getConstructor(JSONObject.class).newInstance(delivery);

    int index = list.indexOf(c);
    // Verificar que en la lista de estados haya elementos
    Status lastStatus = c.getStatuses().getLast();
    System.out.println(lastStatus);
    //Validaciones
    if (lastStatus.getDeliveryStatus() == DeliveryStatus.RECEIVED) {
      throw new NoSuchElementException("Cannot delete a shipment in received status");
    }
    if (lastStatus.getDeliveryStatus() == DeliveryStatus.DELIVERED) {
      throw new NoSuchElementException("It is not possible to delete a status of a delivered shipment if it has not been returned.");
    }
    if (lastStatus.getDeliveryStatus() == DeliveryStatus.RETURNED) {
      throw new NoSuchElementException("Cannot delete a status of a returned shipment");
    }
    //Eliminar el ultimo estado
    c.getStatuses().removeLast();

    //Actualizar en la lista el envio
    list.set(index, c);

    //Reescribir el archivo
    Utils.writeJSON(list, fileName);
    // retorne una instancia de JSONObject con los atributos:
    return new JSONObject().put("message", "ok").put("data", delivery);
  }

  @SuppressWarnings("uncheked")
  @Override
  public Class<Delivery> getDataType() {
    return (Class<Delivery>) subclass;
  }

}