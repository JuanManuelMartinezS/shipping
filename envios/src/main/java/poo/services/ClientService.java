package poo.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import poo.helpers.Utils;
import poo.model.Client;

public class ClientService implements Service<Client> {
  // Operaciones CRUD (Create Reload Update Delete)
  private List<Client> list;
  private final String fileName;

  public ClientService() throws Exception {
    fileName = Utils.PATH + "Client.json";

    if (Utils.fileExists(fileName)) {
      load();
    } else {
      list = new ArrayList<>();
    }
  }

  @Override
  public JSONObject add(String strJson) throws Exception {
    Client c = dataToAddOk(strJson);

    if (list.add(c)) {
      Utils.writeJSON(list, fileName);
    }
    return new JSONObject().put("message", "ok").put("data", c.toJSONObject());
  }

  @Override
  public JSONObject get(int index) {
    return list.get(index).toJSONObject();
  }

  @Override
  public JSONObject get(String id) throws Exception {
    Client c = getItem(id);
    if (c == null) {
      throw new NoSuchElementException(String.format("No client found with ID %s", id));
    }
    return c.toJSONObject();
  }

  @Override
  public Client getItem(String id) throws Exception {
    int i = list.indexOf(new Client(id));
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
      return Utils.keyValueToJson("message", "No access to client data", "error", e.getMessage());
    }
  }

  @Override
  public final List<Client> load() throws Exception {
    list = new ArrayList<>();
    JSONArray jsonArr = new JSONArray(Utils.readText(fileName));
    for (int i = 0; i < jsonArr.length(); i++) {
      JSONObject jsonObj = jsonArr.getJSONObject(i);
      list.add(new Client(jsonObj));
    }
    return list;
  }

  @Override
  public JSONObject update(String id, String strJson) throws Exception {
    // Crear un json object con las claves y valores a actualizar
    JSONObject newData = new JSONObject(strJson);
    // buscar el Client que se debe actualizar y recordar la posicion
    Client client = getItem(id);

    if (client == null) {
      throw new NullPointerException("Client not found" + id);
    }
    int i = list.indexOf(client);
    client = getUpdated(newData, client);
    list.set(i, client);
    // actualizar el archivo de Clients
    Utils.writeJSON(list, fileName);
    // devolver el Client con los cambios realizados
    return new JSONObject().put("message", "ok").put("data", client.toJSONObject());
  }

  @Override
  public Client getUpdated(JSONObject newData, Client current) {
    JSONObject updated = new JSONObject(current);

    if (newData.has("name")) {
      updated.put("name", Utils.stringOk("name", 4, newData));
    }

    if (newData.has("address")) {
      updated.put("address", Utils.stringOk("address", 10, newData));
    }

    if (newData.has("phoneNumber")) {
      updated.put("phoneNumber", Utils.stringOk("phoneNumber", 10, newData));
    }

    if (newData.has("city")) {
      updated.put("city", Utils.stringOk("city", 4, newData));
    }

    return new Client(updated);
  }

  @Override
  public JSONObject remove(String id) throws Exception {
    JSONObject client = get(id);
    if (get(id) == null) {
      throw new NoSuchElementException(String.format("No client found with ID %s", id));
    }
    Client c = new Client(client);
    // Verificar si el cliente existe
    exists(client);
    // Eliminar de list el cliente c
    if (!list.remove(c)) {
      throw new Exception(String.format("Client with ID %s could not be removed", id));
    }
    list.remove(c);
    // Actualizar el archivo de clientes
    Utils.writeJSON(list, fileName);
    // retorne una instancia de JSONObject con los atributos:
    return new JSONObject().put("message", "ok").put("data", client);
  }

  private void exists(JSONObject client) throws Exception {
    String name = client.getString("name");

    // Buscar el cliente en mercancia y si no existe permitir eliminarlo
    if (Utils.exists(Utils.PATH + "Merchandise", "storer", client)) {
      throw new Exception(String.format("Not deleted, client %s has merchandises on record", name));
    }
    // Buscar el cliente en envios y si existe no permitir eliminarlo
    exists("Pack", client);
    exists("Envelope", client);
    exists("Box", client);
    exists("Sack", client);

  }

  private void exists(String filename, JSONObject client) throws Exception {
    if (Utils.exists(Utils.PATH + filename, "sender", client)) {
      throw new Exception(String.format("Not deleted. Client %s is registered in shipments of type %s",
          client.getString("name"), filename));
    }
    if(Utils.exists(Utils.PATH + filename, "addressee", client)){
      throw new Exception(String.format("Not deleted. Client %s is registered in shipments of type %s",
          client.getString("name"), filename));
    }
  }

  @Override
  public Client dataToAddOk(String strJson) throws Exception {
    JSONObject json = new JSONObject(strJson);
    if (!json.has("id") || json.getString("id").isBlank()) {
      json.put("id", Utils.getRandomKey(5));
    }
    Utils.stringOk("id", 5, json);
    Utils.stringOk("name", 1, json);
    Utils.stringOk("address", 10, json);
    Utils.stringOk("phoneNumber", 10, json);
    Utils.stringOk("city", 4, json);

    Client c = new Client(json);

    if (list.contains(c)) {
      throw new ArrayStoreException(String.format("El cliente %s . %s ya existe", c.getId(), c.getName()));
    }
    return c;
  }

  @Override
  public JSONObject size() {
    return new JSONObject().put("size", list.size()).put("message", "ok");
  }

  @Override
  public Class<Client> getDataType() {
    return Client.class;
  }

  public void info() {
    System.out.println(String.format("Creada la instancia %s", getDataType().getSimpleName()));
  }

  @Override
  public JSONObject removeStatus(String numGuide) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'removeStatus'");
  }

}
