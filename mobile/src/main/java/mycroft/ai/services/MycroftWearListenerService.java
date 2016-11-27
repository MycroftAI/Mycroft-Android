package mycroft.ai.services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import mycroft.ai.MainActivity;

/**
 * Created by jpoff on 9/7/2016.
 */
public class MycroftWearListenerService extends WearableListenerService {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private static final String TAG = "Mycroft";
    public static final String MYCROFT_QUERY_MESSAGE_PATH = "/mycroft_query";

    public static final String MYCROFT_WEAR_REQUEST ="mycroft.ai.wear.request";
    public static final String MYCROFT_WEAR_REQUEST_MESSAGE ="mycroft.ai.wear.request.message";

    private LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String message = new String(messageEvent.getData());

        if (messageEvent.getPath().equals(MYCROFT_QUERY_MESSAGE_PATH)) {
            Log.d(TAG, "MycroftWearRequest Message: " + message);

            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.putExtra("MYCROFT_WEAR_REQUEST", message);
            startActivity(startIntent);

            handoffWearRequest(message);
        }
    }

    @Override
    public void onPeerConnected(Node node){
        Log.d(TAG, "onPeerConnected");
    }

    private void handoffWearRequest(String message) {
        Log.d(TAG, "Hand Off Wear Request");

        if (message != null) {
            Intent intent = new Intent(MYCROFT_WEAR_REQUEST);
            intent.putExtra(MYCROFT_WEAR_REQUEST_MESSAGE, message);
            localBroadcastManager.sendBroadcast(intent);
        }
    }
}


