package poo.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.json.JSONObject;

import poo.helpers.Utils;

public class Merchandise implements Exportable, Service {
    private LocalDateTime dayOfArrival;
    private LocalDateTime dayOfDeparture;
    private String content;
    private String warehouse;
    private String id;
    private double width;
    private double height;
    private double length;
    private Client storer;

    // Constructor por defecto
    public Merchandise() {
        this("null", 0.0, 0.0, 0.0, LocalDateTime.now(),
                LocalDateTime.now(), "null", new Client(), "null");
    }

    // Constructo parametrizado
    public Merchandise(String content, double width, double height, double length, LocalDateTime dayOfArrival,
            LocalDateTime dayOfDeparture,
            String warehouse, Client storer, String id) {
        setDayOfArrival(dayOfArrival);
        setDayOfDeparture(dayOfDeparture);
        setContent(content);
        setWarehouse(warehouse);
        setId(id);
        setWidth(width);
        setHeight(height);
        setLength(length);
        setStorer(storer);
    }

    // Constructor que solo recibe el id
    public Merchandise(String id) {
        this();
        setId(id);
    }

    // Constructor que no recibe id
    public Merchandise(String content, double width, double height, double length, LocalDateTime dayOfArrival,
            LocalDateTime dayOfDeparture,
            String warehouse, Client storer) {
        this(content, width, height, length, dayOfArrival, dayOfDeparture, warehouse, storer, Utils.getRandomKey(6));
    }

    // Constructor que recibe un objeto tipo Merchandise
    public Merchandise(Merchandise m) {
        this(m.content, m.width, m.height, m.length, m.dayOfArrival, m.dayOfDeparture,
                m.warehouse, m.storer, m.id);
    }

    // Constructor que recibe un objeto tipo JSON
    public Merchandise(JSONObject json) {
        this(json.getString("content"),
                json.getDouble("width"),
                json.getDouble("height"),
                json.getDouble("length"),
                LocalDateTime.parse(json.getString("dayOfArrival")),
                LocalDateTime.parse(json.getString("dayOfDeparture")),
                json.getString("warehouse"),
                new Client(json.getJSONObject("storer")),
                json.getString("id"));

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
        return this.id.equals(((Merchandise) obj).id);

    }

    // Metodo que obtiene el volumen de una caja, se usa en getPayment
    public double getVolume() {
        double volume = this.width * this.length * this.height;
        return Math.round(volume * 10) / 10.0;
    }
    // Metodo sobreescrito de la interfaz Service que calcula el pago dependiendo la
    // cantidad de dias almacenado
    @Override
    public double getPayment() {
        double volume = getVolume();
        Duration time = Duration.between(this.dayOfArrival, this.dayOfDeparture);
    
        double days = time.getSeconds() / 86400.0;
        double payment = days * (volume * 5000);
    
        // Redondea a un decimal
        return Math.round(payment * 10) / 10.0;
    }

    public LocalDateTime getDayOfArrival() {
        return this.dayOfArrival;
    }

    public final void setDayOfArrival(LocalDateTime dayOfArrival) {
        this.dayOfArrival = dayOfArrival;
    }

    public LocalDateTime getDayOfDeparture() {
        return this.dayOfDeparture;
    }

    public final void setDayOfDeparture(LocalDateTime dayOfDeparture) {
        this.dayOfDeparture = dayOfDeparture;
    }

    public String getContent() {
        return this.content;
    }

    public final void setContent(String content) {
        this.content = content;
    }

    public String getWarehouse() {
        return this.warehouse;
    }

    public final void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getId() {
        return this.id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public double getWidth() {
        return this.width;
    }

    public final void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return this.height;
    }

    public final void setHeight(double height) {
        this.height = height;
    }

    public double getLength() {
        return this.length;
    }

    public final void setLength(double length) {
        this.length = length;
    }

    public Client getStorer() {
        return this.storer;
    }

    public final void setStorer(Client storer) {
        this.storer = storer;
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

}
