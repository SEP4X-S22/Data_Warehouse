package com.Apharma.sep4.MiddlePoint;

import com.Apharma.sep4.DAO.ReadingDAO;
import com.Apharma.sep4.Model.DownLinkPayload;
import com.Apharma.sep4.Run.WebSocketClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;

//Date formatting method "tsToString" taken from Ib Havn

@Component
public class MiddlePointDecoder
{
  private JSONObject receivedPayload; // JSONObject so we can extract the data more easily?
  @Autowired
  private ReadingDAO readingDAO;
  private String telegram;
  
  public MiddlePointDecoder()
  {
  
  }
  
  public JSONObject getReceivedPayload()
  {
    return receivedPayload;
  }
  
  public void setReceivedPayload(String receivedPayload)
  {
    try
    {
      this.receivedPayload = new JSONObject(receivedPayload);
      doIt();
      createTelegram();
    }
    catch (JSONException e)
    {
      e.printStackTrace();
    }
  }
  
  public void doIt()
  {
    decode(receivedPayload);
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
        String roomId = receivedPayload.getString("EUI");
  
        long ts = receivedPayload.getLong("ts");
        String formattedStringDate = tsToString(ts);
  
        //Date still hardcoded - since we have to change everything else ... :(
        //Date timestamp = new Date((ts + (3600 * 2 * 1000)));
        
        readingDAO.storeNewEntry(hum, tempFinal, co2, formattedStringDate, roomId);
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
  
  public String encode(int min, int max){
    //    DownLinkPayload downLinkPayload = new DownLinkPayload();
    //    downLinkPayload.setCmd("tx");
    //    downLinkPayload.setEUI("0004A30B00E7E072");
    //    downLinkPayload.setPort(1);
    //    downLinkPayload.setConfirmed(true);
    //    downLinkPayload.setData("0102AABB");
    
    return "";
  }
  
  public void createTelegram()
  {
    DownLinkPayload downLinkPayload = new DownLinkPayload();

    int port = 0;
    String roomId;
    try
    {
      roomId = receivedPayload.getString("EUI");
      port = receivedPayload.getInt("port");
      downLinkPayload.setEUI(roomId);
      downLinkPayload.setPort(port);
      downLinkPayload.setCmd("tx");
      downLinkPayload.setConfirmed(true);
      downLinkPayload.setData("0102AABB");
    }
    catch (JSONException e)
    {
      e.printStackTrace();
    }

    

//    downLinkPayload.setEUI("0004A30B00E7E072");
//    downLinkPayload.setPort(1);

    
    convertToObjectToJson(downLinkPayload);
  }
  
  public String getTelegram()
  {
    return telegram;
  }
  
  public void setTelegram(String telegram)
  {
    this.telegram = telegram;
  }
  
  public void convertToObjectToJson(DownLinkPayload downLinkPayload){
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
  }
}
