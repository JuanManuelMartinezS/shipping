package poo.model;

import java.util.ArrayList;

import org.json.JSONObject;

public class Box extends Delivery {
    private double width;
    private double height;
    private double length;

    /** Constructor por defecto */
    public Box() {
        super();
        setWidth(0.0);
        setHeight(0.0);
        setLength(0.0);

    }

    /** Constructor con parámetros */
    public Box(boolean isFragile, String content, double value, ArrayList<Status> statuses, double weight,
            String numGuide, Client sender, Client addressee, double width, double height, double length) {
        super(isFragile, content, value, statuses, weight, numGuide, sender, addressee);
        setWidth(width);
        setHeight(height);
        setLength(length);
    }

    /** Constructor que recibe un objeto tipo Box */
    public Box(Box b) {
        super(b);
        setWidth(b.width);
        setHeight(b.height);
        setLength(b.length);

    }

    /** Constructor que no recibe numero de guia */
    public Box(boolean isFragile, String content, double value, ArrayList<Status> status, double weight,
            Client sender, Client addressee, double width, double height, double length) {
        super(isFragile, content, value, status, weight, sender, addressee);
        setWidth(width);
        setHeight(height);
        setLength(length);
    }

    /** Constructor que solo recibe numero de guia */
    public Box(String numGuide) {
        super(numGuide);

    }

    /** Constructor que recibe un objeto tipo JSON */
    public Box(JSONObject json) {
        super(json);
        setWidth(json.getDouble("width"));
        setHeight(json.getDouble("height"));
        setLength(json.getDouble("length"));
    }

    // Metodo de la interfaz exportable
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
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
        return this.numGuide.equals(((Box) obj).numGuide);

    }

    // Metodo que obtiene el volumen de una caja, se usa en getPayment
    public double getVolume() {
        double volume = this.width * this.length * this.height;
        return Math.round(volume * 10) / 10.0;
    }
    // Metodos de la interfaz Service de model
    @Override
    public double getPayment() {
        double volume = getVolume();
        if (volume <= 0) {
            throw new IllegalArgumentException("El volumen no puede ser menor o igual a cero");
        } else if (getVolume() <= 0.5) {
            return 10000 + (500 * this.weight);
        } else if (getVolume() <= 1.0) {
            return 12000 + (500 * this.weight);
        } else if (getVolume() <= 3.0) {
            return 15000 + (500 * this.weight);
        } else if (getVolume() <= 6.0) {
            return 25000 + (500 * this.weight);
        } else if (getVolume() <= 10.0) {
            return 30000 + (500 * this.weight);
        } else {
            return 10000 * (getVolume() / 10) + (500 * this.weight);
        }
    }

    @Override
    public String toJSON() {
        return this.toJSONObject().toString();
    }

    // Getters y Setters
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

}
