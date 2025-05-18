package poo.model;

import java.util.ArrayList;

import org.json.JSONObject;

public class Envelope extends Delivery {
    private boolean isCertified;

    /** Constructor por defecto */
    public Envelope() {
        super();
        setIsCertified(false);

    }

    /** Constructor con parámetros */
    public Envelope(boolean isFragile, String content, double value, ArrayList<Status> statuses, double weight,
            String numGuide, Client sender, Client addressee, boolean isCertified) {
        super(isFragile, content, value, statuses, weight, numGuide, sender, addressee);
        setIsCertified(isCertified);

    }

    /** Constructor que recibe un objeto tipo Envelope */
    public Envelope(Envelope p) {
        super(p);
        setIsCertified(p.isCertified);
    }

    /** Constructor que no recibe numero de guia */
    public Envelope(boolean isFragile, String content, double value, ArrayList<Status> status, double weight,
            Client sender, Client addressee) {
        super(isFragile, content, value, status, weight, sender, addressee);
    }

    /** Constructor que solo recibe numero de guia */
    public Envelope(String numGuide) {
        super(numGuide);
    }

    /** Constructor que recibe un objeto tipo JSON */
    public Envelope(JSONObject json) {
        super(json);
        setIsCertified(json.getBoolean("isCertified"));
    }

    // Getters y Setters
    public boolean getIsCertified() {
        return this.isCertified;
    }

    public final void setIsCertified(boolean isCertified) {
        this.isCertified = isCertified;
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
        return this.numGuide.equals(((Envelope) obj).numGuide);

    }

    // Metodos de la interfaz Service de model
    @Override
    public String toJSON() {
        return this.toJSONObject().toString();
    }

    @Override
    public double getPayment() {
        double cobro = (1300000 / 1000) * 2;
        if (isCertified) {
            return cobro + cobro * 0.10;
        }
        return cobro;

    }
}