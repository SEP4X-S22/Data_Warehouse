package com.Apharma.sep4.MiddlePoint;

import com.Apharma.sep4.Model.DownlinkPayload;
import com.Apharma.sep4.Persistence.DAO.ReadingDAO;
import com.Apharma.sep4.Persistence.DAO.iReadingDAO;
import com.Apharma.sep4.Run.WebSocketClient;
import com.Apharma.sep4.WebAPI.Repos.SensorRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

//Date formatting method "tsToString" courtesy of Ib Havn

@Component
public class MiddlePointDecoder
{
  private JSONObject receivedPayload = null;
  @Autowired
  private ReadingDAO readingDAO;
  private String telegram = null;
	private WebSocketClient client;
  @Autowired
  private SensorRepo sensorRepo;

  private String log = "";




  public MiddlePointDecoder(@Lazy WebSocketClient client)
  {
		this.client = client;
  }

  public JSONObject getReceivedPayload()
  {
    return receivedPayload;
  }

	public void setReceivedPayload(String payload)
  {
    try
    {
        receivedPayload = new JSONObject(payload);
        decode(receivedPayload);
    }
    catch (JSONException e)
    {
      e.printStackTrace();
    }
  }



  public void decode(JSONObject receivedPayload)
  {
    try
    {
      if (receivedPayload.getString("cmd").equals("rx"))
      {
        String data = receivedPayload.getString("data");
        int hum = Integer.parseInt(data, 0, 2, 16); // radix describes the base we want our number in. 16 - hex, so on
        int temp = Integer.parseInt(data, 3, 6, 16);
        double tempFinal = temp / 10d;
        int co2 = Integer.parseInt(data, 7, 10, 16);
        int light = Integer.parseInt(data, 11,14,16);  //---------light measurement decoding
        String roomId = receivedPayload.getString("EUI");
  
        long ts = receivedPayload.getLong("ts");
        String formattedStringDate = tsToString(ts);

        readingDAO.storeNewEntry(hum, tempFinal, co2, light, formattedStringDate, roomId);
      }
    }
    catch (JSONException e)
    {
      e.printStackTrace();
    }
  }
  
  private String tsToString(long ts)
  {
    //TODO add reference to Ib for date changing code
    Date date = new Date(ts); // convert seconds to milliseconds
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy | HH:mm:ss"); // the format of your date
    return dateFormat.format(date);
  }
  
  public void createTelegram(int sensorId, int min, int max)
  {
    String roomId = sensorRepo.getRoomIdBySensorId(sensorId);
    DownlinkPayload downLinkPayload = new DownlinkPayload();

    String minConstraint = String.format("%04X", min & 0x0FFFFF);
    String maxConstraint = String.format("%04X", max & 0x0FFFFF);
    String data = minConstraint + maxConstraint;

		downLinkPayload.setEUI(roomId);
		downLinkPayload.setPort(1);
		downLinkPayload.setCmd("tx");
		downLinkPayload.setConfirmed(false);
		downLinkPayload.setData(data);

		convertToObjectToJson(downLinkPayload);
    sendDownLink(getTelegram());
  }
  
  public String getTelegram()
  {
    return telegram;
  }
  
  public void setTelegram(String telegram)
  {
    this.telegram = telegram;
  }
  
  public void convertToObjectToJson(DownlinkPayload downLinkPayload){
    String json = null;
    try
    {
      json = new ObjectMapper().writeValueAsString(downLinkPayload);
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }
    setTelegram(json);

		System.out.println(json);

  }

  private void sendDownLink(String json){
    client.sendDownLink(json);
  }
//--------------------------------------
  public void setLog(String payload)
  {
    String good = formatter(payload);
    String date = tsToString(System.currentTimeMillis() + 2*3600*1000);
    String prefix;
    if(payload.contains("\"rx\"") || payload.contains("\"tx\"") || payload.contains("\"txd\""))
    {
      if (payload.contains("\"tx\""))
      {
        prefix = "DOWNLINK Message (From Android)";
      }
      else if (payload.contains("\"txd\""))
      {
        prefix = "UPLINK Message (Confirmation message for Android)";
      }
      else{
        prefix = "UPLINK Message (From IoT)";
      }
      log = log + "<br> <b style=\"color:#008F11;\">" + date + " - " + prefix + "</b><p style=\"color:#00FF41;\">" + good + "</p>  ";
    }
  }

  public String getLog()
  {
    return
        "<html> <head> <h3 style=\"color:#003B00;\"> PAYLOAD LOGGER </h3></head><body style=\"background-color:black;\"> "
            + log + "</body></html>";
  }

  public String formatter(String payload){
    payload = payload.replace("{", "{<br>&nbsp&nbsp&nbsp&nbsp");
    payload = payload.replace(",", ",<br>&nbsp&nbsp&nbsp&nbsp");
    payload = payload.replace("}","<br>}<br>");
    return payload;
  }

  public void clearLog(){
    log = "";
  }
  //--------------------------------
}

