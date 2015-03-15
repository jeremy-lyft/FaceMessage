package com.example.jeremy.facemessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
//import com.getpebble.android.kit.PebbleKit;
//import com.getpebble.android.kit.util.PebbleDictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver nlservicereciver;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("EC7EE5C6-8DDF-4089-AA84-C3396A11CC95");

    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.jeremy.facemessage.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }
//    public void sendSentimentAndMessage(String sentiments, String message) {
//        boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
//        Log.i(getLocalClassName(), "Pebble is " + (connected ? "connected" : "not connected"));
//
//        PebbleDictionary data = new PebbleDictionary();
//
//        // Add a key of 0, and a uint8_t (byte) of value 42.
//        data.addUint8(0, (byte) 42);
//
//        data.addString("message", message);
//        data.addString("sentiment", sentiments);
//        PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
//    }

    public String getSentiment(String message) throws IOException {
        String sentiment = "";
        try {
            System.out.println("making request");
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://access.alchemyapi.com/calls/text/TextGetTextSentiment");

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("apikey", "2c1092552a7bf9a70bedf8b67809d542c5889c7d"));
            pairs.add(new BasicNameValuePair("text", message));
            pairs.add(new BasicNameValuePair("outputMode", "json"));
            post.setEntity(new UrlEncodedFormEntity(pairs));

            HttpResponse response = client.execute(post);
            System.out.println("after request");
            String str = EntityUtils.toString(response.getEntity());
            JSONObject j = new JSONObject(str);
            sentiment = j.getJSONObject("docSentiment").getString("score");
        }
        catch (ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sentiment;
    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.d(TAG,"**********  onNotificationPosted");
        Log.d(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent i = new  Intent("com.example.jeremy.facemessage.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event", "Received notification, begin processing\n");
        sendBroadcast(i);

        String message = sbn.getNotification().tickerText.toString().split(":")[1];
        Log.d(TAG, message);

        i.putExtra("notification_event", "Message is: " + message + "\n");
        sendBroadcast(i);

        String sentiments = "Happy!";
        try {
            Log.d(TAG, "before request");
            sentiments = this.getSentiment(message);
        } catch (IOException e) {
            sentiments = "crashed;";
        }

        i.putExtra("notification_event", "Sentiment is: " + sentiments + "\n");
        sendBroadcast(i);

        //this.sendSentimentAndMessage(sentiments, message);

        i.putExtra("notification_event", "==========Done! Sent to watch!===========\n");
        sendBroadcast(i);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    class NLServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("command").equals("clearall")){
                Log.d("FaceMessage", "clearall called");
                NLService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("com.example.jeremy.facemessage.NOTIFICATION_LISTENER_EXAMPLE");
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                int i=1;
                for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                    Intent i2 = new  Intent("com.example.jeremy.facemessage.NOTIFICATION_LISTENER_EXAMPLE");
                    i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent("com.example.jeremy.facemessage.NOTIFICATION_LISTENER_EXAMPLE");
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }

}
