package poo.model;

import java.util.ArrayList;

import org.json.JSONObject;

public class Sack extends Delivery {

    /** Constructor por defecto */
    public Sack() {
        super();

    }

    /** Constructor con parámetros */
    public Sack(boolean isFragile, String content, double value, ArrayList<Status> status, double weight,
            String numGuide, Client sender, Client addressee) {
        super(isFragile, content, value, status, weight, numGuide, sender, addressee);
    }

    /** Constructor que recibe un objeto tipo Pack */
    public Sack(Sack p) {
        super(p);
    }

    /** Constructor que no recibe numero de guia */
    public Sack(boolean isFragile, String content, double value, ArrayList<Status> status, double weight,
            Client sender, Client addressee) {
        super(isFragile, content, value, status, weight, sender, addressee);
    }

    /** Constructor que solo recibe numero de guia */
    public Sack(String numGuide) {
        super(numGuide);
    }

    /** Constructor que recibe un objeto tipo JSON */
    public Sack(JSONObject json) {
        super(json);
    }

    // Metodos de la interfaz exportable
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
        return this.numGuide.equals(((Sack) obj).numGuide);

    }

    // Metodos de la interfaz Service
    @Override
    public double getPayment() {
        return 1000 * this.weight;
    }

    // Metodos de la interfaz Service de model
    @Override
    public String toJSON() {
        return this.toJSONObject().toString();
    }

}
