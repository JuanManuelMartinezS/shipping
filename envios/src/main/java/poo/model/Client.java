package poo.model;

import org.json.JSONObject;

import poo.helpers.Utils;

public class Client implements Exportable {
    private String id;
    private String name;
    private String address;
    private String phoneNumber;
    private String city;

    // Constructor por defecto
    public Client() {
        this("", "", "", "", "");
    }

    // Constructor con parámetros
    public Client(String id, String name, String address, String phoneNumber, String city) {
        setId(id);
        setName(name);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setCity(city);
    }

    // Constructor que recibe un id
    public Client(String id) {
        this();
        setId(id);
    }

    // Constructor que no recibe id y coloca un id aleatorio
    public Client(String name, String address, String phoneNumber, String city) {
        this(Utils.getRandomKey(5), name, address, phoneNumber, city);
    }

    // Constructor que recibe un objeto tipo Client
    public Client(Client c) {
        this(c.id, c.name, c.address, c.phoneNumber, c.city);
    }

    // Constructor que recibe un objeto tipo JSONObject
    public Client(JSONObject json) {
        this(json.getString("id"), json.getString("name"), json.getString("address"), json.getString("phoneNumber"),
                json.getString("city"));
    }

    // funcion que determina si un objeto es igual a otro solamente comparando su id
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return this.id.equals(((Client) obj).id);

    }

    // Metodo de la interfaz Exportable
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }

    public String getId() {
        return this.id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return this.address;
    }

    public final void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public final void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return this.city;
    }

    public final void setCity(String city) {
        this.city = city;
    }

}
