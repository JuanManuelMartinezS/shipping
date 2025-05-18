package poo.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import poo.helpers.Utils;

public abstract class Delivery implements Exportable, Service {
    protected boolean isFragile;
    protected String content;
    protected double value;
    protected double weight;
    protected String numGuide;
    protected Client sender;
    protected Client addressee;
    protected ArrayList<Status> statuses;

    /** Constructor por defecto */
    public Delivery() {
        this(false, "", 0.0, new ArrayList<>(), 0.0, "", new Client(), new Client());

    }

    /** Constructor con parametros */
    public Delivery(boolean isFragile, String content, double value, ArrayList<Status> statuses, double weight,
            String numGuide, Client sender, Client addressee) {
        setIsFragile(isFragile);
        setContent(content);
        setValue(value);
        setStatuses(statuses);
        setWeight(weight);
        setNumGuide(numGuide);
        setSender(sender);
        setAddressee(addressee);
    }

    /** Constructor que solo recibe el numero de la guia */
    public Delivery(String numGuide) {
        this();
        setNumGuide(numGuide);
    }

    /** Constructor que no recibe un numero de guia */
    public Delivery(boolean isFragile, String content, double value, ArrayList<Status> statuses, double weight,
            Client sender, Client addressee) {
        this(isFragile, content, value, statuses, weight, Utils.getRandomKey(7), sender, addressee);
    }

    /** Constructor Copia, que recibe un objeto de tipo Delivery */
    public Delivery(Delivery d) {
        this(d.isFragile, d.content, d.value, d.statuses, d.weight, d.sender,
                d.addressee);
    }

    // Constructor que recibe objeto json
    /**
     * Recuperar los estados en una instancia de JSONArray, llamada jsonstatuses *
     * Recorrer el jsonstatuses del paso anterior y en cada pasada agregar a
     * this.estados lo que devuekva jsonstatuses.getJSONObject(i), pero convertido a
     * una instancia de estado
     */
    public Delivery(JSONObject json) {
        this(
                json.getBoolean("isFragile"),
                json.getString("content"),
                json.getDouble("value"),
                new ArrayList<>(),
                json.getDouble("weight"),
                json.getString("numGuide"),
                new Client(json.getJSONObject("sender")),
                new Client(json.getJSONObject("addressee")));
        JSONArray jsonstatuses = json.getJSONArray("statuses");
        for (int i = 0; i < jsonstatuses.length(); i++) {
            this.statuses.add(new Status(jsonstatuses.getJSONObject(i)));
        }

    }

    // funcion que determina si un objeto es igual a otro solamente comparando su
    // numero de Guia
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
        return this.numGuide.equals(((Delivery) obj).numGuide);

    }

    // Metodos de la interfaz Service de model
    @Override
    public String toJSON() {
        return this.toJSONObject().toString();
    }

    // Metodo de exportable
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
    }

    @Override
    public String toString() {
        String isFragile1 = this.isFragile ? "Si" : "No";
        return String.format("%s %s:%s %s:%s %s %d %d %s %s", numGuide, sender.getId(), sender.getName(),
                addressee.getId(), addressee.getName(), content, weight, value, isFragile1,
                statuses.get(statuses.size() - 1).toString());
    }

    @Override
    public double getPayment() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Metodo que obtiene el tipo de la clase
     * 
     * @return this.getClass().getSimpleName();
     */
    public String getType() {
        return this.getClass().getSimpleName();
    }
    // Getters y Setters

    public boolean getIsFragile() {
        return this.isFragile;
    }

    public final void setIsFragile(boolean isFragile) {
        this.isFragile = isFragile;
    }

    public String getContent() {
        return this.content;
    }

    public final void setContent(String content) {
        this.content = content;
    }

    public double getValue() {
        return this.value;
    }

    public final void setValue(double value) {
        this.value = value;
    }

    public double getWeight() {
        return this.weight;
    }

    public final void setWeight(double weight) {
        this.weight = weight;
    }

    public String getNumGuide() {
        return this.numGuide;
    }

    public final void setNumGuide(String numGuide) {
        this.numGuide = numGuide;
    }

    // Agregue a la clase Envio un método String getId() que retorne el número de
    // guía
    public String getId() {
        return this.numGuide;
    }

    public Client getSender() {
        return this.sender;
    }

    public final void setSender(Client sender) {
        this.sender = sender;
    }

    public Client getAddressee() {
        return this.addressee;
    }

    public final void setAddressee(Client addressee) {
        this.addressee = addressee;
    }

    public boolean isIsFragile() {
        return this.isFragile;
    }

    public ArrayList<Status> getStatuses() {
        return this.statuses;
    }

    public final void setStatuses(ArrayList<Status> statuses) {
        this.statuses = statuses;
    }

}
