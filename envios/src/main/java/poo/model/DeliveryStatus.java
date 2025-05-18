package poo.model;

import org.json.JSONArray;
import org.json.JSONObject;

public enum DeliveryStatus {
    RECEIVED("Received"), IN_PREPARATION("In preparation"),
    SENT("Sent"), ON_THE_WAY("On the way"), DELIVERED("Delivered"), RETURNED("Returned"),

    LOST("Lost"), RESHIPPED("Reshipped"), AVAILABLE_IN_OFFICE("Available in office");

    private final String value;

    private DeliveryStatus(String value) {
        this.value = value;
    }

    /**
     * Devuelve el valor de un constante enumerada en formato humano
     * Ejemplo: System.out.println(tp.getValue()); // devuelve: Idioma español
     * 
     * @return El valor del argumento value, recibido por el constructor
     */
    public String getValue() {
        return value;
    }

    /**
     * Dado un string, devuelve la constante enumerada correspondiente. Ejemplo:
     * Language.getEnum("Idioma español") devuelve Language.SPANISH
     * no confundir con Language.valueOf("CONSTANTE_ENUMERADA")
     * 
     * @param value La expresión para humanos correspondiente a la constante
     * @return La constante enumerada
     */
    public static DeliveryStatus getEnum(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        for (DeliveryStatus l : values()) {
            if (value.equalsIgnoreCase(l.getValue())) {
                return l;
            }
        }
        throw new IllegalArgumentException();
    }

    public static JSONObject getAll() {
        JSONArray jsonArr = new JSONArray();
        for (DeliveryStatus v : values()) {
            jsonArr.put(
                    new JSONObject()
                            .put("ordinal", v.ordinal())
                            .put("key", v)
                            .put("value", v.value));
        }
        return new JSONObject().put("message", "ok").put("data", jsonArr);
    }

}
