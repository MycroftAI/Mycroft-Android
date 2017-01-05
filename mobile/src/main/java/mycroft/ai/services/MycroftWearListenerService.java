/*
 *  Copyright (c) 2017. Mycroft AI, Inc.
 *
 *  This file is part of Mycroft-Android a client for Mycroft Core.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mycroft.ai.services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import mycroft.ai.MainActivity;
import mycroft.ai.shared.wear.Constants;


/**
 * Created by jpoff on 9/7/2016.
 */
public class MycroftWearListenerService extends WearableListenerService {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private static final String TAG = "Mycroft";


    private LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String message = new String(messageEvent.getData());

        if (messageEvent.getPath().equals(Constants.MYCROFT_QUERY_MESSAGE_PATH)) {
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
            Intent intent = new Intent(Constants.MYCROFT_WEAR_REQUEST);
            intent.putExtra(Constants.MYCROFT_WEAR_REQUEST_MESSAGE, message);
            localBroadcastManager.sendBroadcast(intent);
        }
    }
}


