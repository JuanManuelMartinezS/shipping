package poo.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pack extends Delivery {

    /** Constructor por defecto */
    public Pack() {
        super();

    }

    /** Constructor con parámetros */
    public Pack(boolean isFragile, String content, double value, ArrayList<Status> status, double weight,
            String numGuide, Client sender, Client addressee) {
        super(isFragile, content, value, status, weight, numGuide, sender, addressee);
    }

    /** Constructor que recibe un objeto tipo Pack */
    public Pack(Pack p) {
        super(p);
    }

    /** Constructor que no recibe numero de guia */
    public Pack(boolean isFragile, String content, double value, ArrayList<Status> status, double weight,
            Client sender, Client addressee) {
        super(isFragile, content, value, status, weight, sender, addressee);
    }

    /** Constructor que solo recibe numero de guia */
    public Pack(String numGuide) {
        super(numGuide);
    }

    /** Constructor que recibe un objeto tipo JSON */
    public Pack(JSONObject json) {
        super(json);
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
        return this.numGuide.equals(((Pack) obj).numGuide);

    }

    // Metodos de la interfaz Service
    @Override
    public double getPayment() {
        return 1000 * (this.weight / 10);
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
