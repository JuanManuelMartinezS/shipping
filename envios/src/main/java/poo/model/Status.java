package poo.model;

import java.time.LocalDateTime;

import org.json.JSONObject;

public class Status implements Exportable {
    private LocalDateTime dateTime;
    private DeliveryStatus deliveryStatus;

    // Constructor por defecto
    public Status() {
        setDateTime(LocalDateTime.now());
        setDeliveryStatus(DeliveryStatus.RECEIVED);
    }

    // Constructor con parámetros
    public Status(LocalDateTime dateTime, DeliveryStatus deliveryStatus) {
        setDateTime(dateTime);
        setDeliveryStatus(deliveryStatus);
    }

    // Constructor que recibe un objeto tipo status
    public Status(Status s) {
        this(s.getDateTime(), s.getDeliveryStatus());
    }

    // Constructor con parametro JSONObject
    public Status(JSONObject json) {
        // Parser un atributo tipo LocaldateTimeTime, y acceder a un enum a partir de un
        // Objeto JSON
        this(LocalDateTime.parse(json.getString("dateTime")), json.getEnum(DeliveryStatus.class, "deliveryStatus"));
    }

    // Metodo de la interfaz Exportable
    @Override
    public JSONObject toJSONObject() {
        return new JSONObject(this);
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
        return this.deliveryStatus.equals(((Status) obj).deliveryStatus);

    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public final void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public DeliveryStatus getDeliveryStatus() {
        return this.deliveryStatus;
    }

    public final void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /**
     * Agregue a la clase Estado un método String getId() que retorne la cadena de
     * caracteres correspondiente a super.hashCode(), reinicie la aplicación y
     * ejecute un petición HTTP para agregar un paquete, una caja y un bulto.
     */
    public String getId() {
        return String.valueOf(super.hashCode());
    }
    //Modificacion del toString para que retorne el nombre de la clave del enum
    @Override
    public String toString() {
        return getDeliveryStatus().name();
    }
}