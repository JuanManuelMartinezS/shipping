package poo.services;

import org.json.JSONObject;

import poo.model.Client;
import poo.model.Delivery;

public class EnvelopeService extends DeliveryService {

  public EnvelopeService(Class<? extends Delivery> subclass, Service<Client> clients) throws Exception {
    super(subclass, clients);
  }

  @Override
  public JSONObject add(String strJson) throws Exception {
    JSONObject json = new JSONObject(strJson).put("weight", 0).put("isFragile", false).put("value", 0);
    if (!(json.has("content"))) {
      json.put("content", "Documents");
    }
    if (!(json.has("isCertified"))) {
      json.put("isCertified", false);
    }
    return super.add(json.toString());
  }
}
