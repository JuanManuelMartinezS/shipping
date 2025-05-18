package poo.services;

import java.io.IOException;
import java.time.LocalDateTime;
import static java.time.LocalDateTime.parse;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import poo.helpers.Utils;
import poo.model.Client;
import poo.model.Merchandise;

public class MerchandiseService implements Service<Merchandise> {
    private List<Merchandise> list;
    private final String fileName;
    private final Service<Client> clients;

    public MerchandiseService(Service<Client> clients) throws Exception {
        this.clients = clients;
        fileName = Utils.PATH + "Merchandise.json";

        if (Utils.fileExists(fileName)) {
            load();
        } else {
            list = new ArrayList<>();
        }
    }

    @Override
    public JSONObject add(String strJson) throws Exception {
        Merchandise m = dataToAddOk(strJson);
        if (list.add(m)) {
            Utils.writeJSON(list, fileName);
        }
        return new JSONObject().put("message", "ok").put("data", m.toJSONObject());
    }

    @Override
    public JSONObject get(int index) {
        return list.get(index).toJSONObject();
    }

    @Override
    public JSONObject get(String id) throws Exception {
        Merchandise m = getItem(id);
        if (m == null) {
            throw new NoSuchElementException(String.format("Merchandise with id %s not found", id));
        }
        return m.toJSONObject();
    }

    @Override
    public Merchandise getItem(String id) throws Exception {
        int i = list.indexOf(new Merchandise(id));
        return i > -1 ? list.get(i) : null;
    }

    /**
     * Crea un json array con el archivo que se crea, Merchandise.json
     * 
     * @return JSONObject con ok y la data que hay dentro de el archivo
     *         sino
     * @return Lanza la excepcion
     */
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
            return Utils.keyValueToJson("message", "Without access to merchandise data", "error", e.getMessage());
        }
    }

    @Override
    public final List<Merchandise> load() throws Exception {
        list = new ArrayList<>();
        JSONArray jsonArr = new JSONArray(Utils.readText(fileName));
        for (int i = 0; i < jsonArr.length(); i++) {
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            list.add(new Merchandise(jsonObj));
        }
        return list;
    }

    @Override
    public Merchandise dataToAddOk(String strJson) throws Exception {
        JSONObject json = new JSONObject(strJson);
        if (!json.has("id") || json.getString("id").isBlank()) {
            json.put("id", Utils.getRandomKey(8));
        }
        // Nota: las validaciones de si el el string tiene la clave enviada estan en los
        // metodos que realice en Utils (doubleOk, stringOk)
        Utils.stringOk("id", 8, json);

        updateClient(json);

        Utils.stringOk("content", 4, json);
        Utils.stringOk("warehouse", 10, json);
        Utils.doubleOk("width", 0.1, 2.44, json);
        Utils.doubleOk("height", 0.1, 2.59, json);
        Utils.doubleOk("length", 0.1, 12.19, json);

        LocalDateTime dayOfArrival = parse(json.getString("dayOfArrival"));
        LocalDateTime dayOfDeparture = parse(json.getString("dayOfDeparture"));

        if (dayOfArrival.isAfter(dayOfDeparture)) {
            throw new Exception(String.format("Arrival date %s cannot be after Departure date %s",
                    json.getString("dayOfArrival"), json.getString("dayOfDeparture")));
        }
        // Crear una instancia m de tipo Mercancia a partir de json
        Merchandise m = new Merchandise(json);
        // Buscar a m en list y si existe lanzar una ArrayStoreException
        if (list.contains(m)) {
            throw new ArrayStoreException(
                    String.format("The merchandise %s . %s already exists", m.getId(), m.getWarehouse()));
        }
        return m;
    }

    private void updateClient(JSONObject json) throws Exception {
        // Asignar a un string idCliente el valor del atributo cliente de json
        String idStorer = json.getString("storer");
        // En una variable jsonCliente de tipo JSONObject referenciar el cliente que
        // corresponda a idCliente, para esto, use el método get() provisto por
        // clientes.
        JSONObject storer = clients.get(idStorer);
        // En json reemplace el valor del atributo cliente por la instancia obtenida en
        // el
        // paso anterior. Como los objetos se pasan por referencia, no hay necesidad de
        // retornar el json actualizado.
        json.put("storer", storer);
    }

    @Override
    public JSONObject update(String id, String strJson) throws Exception {
        JSONObject newData = new JSONObject(strJson);
        // buscar el Merchandise que se debe actualizar
        Merchandise merchandise = getItem(id);
        int i = list.indexOf(merchandise);
        if (merchandise == null) {
            throw new NullPointerException("Merchandise not found " + id);
        }

        merchandise = getUpdated(newData, merchandise);
        list.set(i, merchandise);
        // actualizar el archivo de Merchandises
        Utils.writeJSON(list, fileName);
        // devolver el Merchandise con los cambios realizados
        return new JSONObject().put("message", "ok").put("data", merchandise.toJSONObject());
    }

    @Override
    public Merchandise getUpdated(JSONObject newData, Merchandise current) throws Exception {
        JSONObject updated = new JSONObject(current);
        // Si newData tiene la llave "cliente" intentar el llamado
        // updateCliente(newData),
        // en caso de presentarse una Exception, lanzar una IllegalArgumentException
        if (newData.has("client")) {
            try {
                updateClient(newData);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Error in determining the customer who is the owner of the merchandise");
            }
        }
        // Validar los datos de newData,
        // Si los datos estan correctos añadir
        if (newData.has("dayOfArrival")) {
            updated.put("dayOfArrival", Utils.stringOk("dayOfArrival", 16, newData));
        }
        if (newData.has("dayOfDeparture")) {
            updated.put("dayOfDeparture", Utils.stringOk("dayOfDeparture", 16, newData));
        }
        if (newData.has("content")) {
            updated.put("content", Utils.stringOk("content", 4, newData));
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
        if (newData.has("warehouse")) {
            updated.put("warehouse", Utils.stringOk("warehouse", 10, newData));
        }
        LocalDateTime dayOfArrival = parse(newData.getString("dayOfArrival"));
        LocalDateTime dayOfDeparture = parse(newData.getString("dayOfDeparture"));
        if (dayOfArrival.isAfter(dayOfDeparture)) {
            throw new Exception(
                    String.format("Arrival date %s cannot be after Departure date %s", dayOfArrival, dayOfDeparture));
        }
        return new Merchandise(updated);
    }

    @Override
    public JSONObject remove(String id) throws Exception {
        JSONObject merchandise = get(id);
        if (get(id) == null) {
            throw new NoSuchElementException(String.format("No merchandise with ID %s found", id));
        }
        Merchandise c = new Merchandise(merchandise);

        // Eliminar de list el la mercancia m
        list.remove(c);
        // Actualizar el archivo de Merchandises
        Utils.writeJSON(list, fileName);
        // retorne una instancia de JSONObject con los atributos:
        return new JSONObject().put("message", "ok").put("data", merchandise);
    }

    @Override
    public Class<Merchandise> getDataType() {
        return Merchandise.class;
    }

    @Override
    public JSONObject size() {
        return new JSONObject().put("size", list.size()).put("message", "ok");
    }

    public void info() {
        System.out.println(String.format("Instance %s created", getDataType().getSimpleName()));
    }

    @Override
    public JSONObject removeStatus(String numGuide) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeStatus'");
    }

}
