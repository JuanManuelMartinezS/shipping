package poo.services;

import org.json.JSONObject;

import poo.helpers.Utils;
import poo.model.Client;
import poo.model.Delivery;

public class BoxService extends DeliveryService {
    public BoxService(Class<? extends Delivery> subclass, Service<Client> clients) throws Exception {
        super(subclass, clients);
    }

    @Override // sobreescribir el método add
    public JSONObject add(String strJson) throws Exception {
        JSONObject json = new JSONObject(strJson);
        if (!(json.has("width"))) {
            json.put("width", 0);
        }
        if (!(json.has("height"))) {
            json.put("height", 0);
        }
        if (!(json.has("length"))) {
            json.put("length", 0);
        }
        Utils.doubleOk("width", 0.1, 2.44, json);
        Utils.doubleOk("height", 0.1, 2.59, json);
        Utils.doubleOk("length", 0.1, 12.19, json);
        return super.add(json.toString());
    }
}
