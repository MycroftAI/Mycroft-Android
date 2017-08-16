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

package mycroft.ai;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.crashlytics.android.Crashlytics;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import mycroft.ai.adapters.MycroftAdapter;
import mycroft.ai.receivers.NetworkChangeReceiver;
import mycroft.ai.shared.utilities.GuiUtilities;
import mycroft.ai.shared.wear.Constants;
import mycroft.ai.utils.NetworkUtil;

import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import static mycroft.ai.Constants.LOCATION_PERMISSION_PREFERENCE_KEY;
import static mycroft.ai.Constants.VERSION_CODE_PREFERENCE_KEY;
import static mycroft.ai.Constants.VERSION_NAME_PREFERENCE_KEY;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "Mycroft";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public WebSocketClient mWebSocketClient;
    private String wsip;

    private int maximumRetries = 1;

    private final int REQ_CODE_SPEECH_INPUT = 100;
    TTSManager ttsManager = null;
    private Switch voxSwitch;


    @NonNull
    private final List<MycroftUtterances> utterances = new ArrayList<>();

    MycroftAdapter ma = new MycroftAdapter(utterances);

    NetworkChangeReceiver networkChangeReceiver;
    BroadcastReceiver wearBroadcastReceiver;

    private boolean isNetworkChangeReceiverRegistered;
    private boolean isWearBroadcastRevieverRegistered;

    RecyclerView recList;

    private SharedPreferences sharedPref;

    boolean launchedFromWidget = false;
    boolean autopromptForSpeech = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });

        voxSwitch = (Switch) findViewById(R.id.voxswitch);

        //attach a listener to check for changes in state
        voxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("appReaderSwitch", isChecked);
                editor.commit();

                // stop tts from speaking if app reader disabled
                if (isChecked == false) {
                    ttsManager.initQueue("");
                }
            }
        });

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        recList.setAdapter(ma);

        registerReceivers();

        ttsManager = new TTSManager(this);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        // start the discovery activity (testing only)
        // startActivity(new Intent(this, DiscoveryActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        boolean consumed = false;
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            consumed = true;
        } else if (id == R.id.action_home_mycroft_ai) {
            Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://home.mycroft.ai"));
            startActivity(intent);
        } else if (id == R.id.action_beacons) {
            Intent intent = new Intent(this, BeaconActivity.class);
            startActivity(intent);
            consumed = true;
        }

        return consumed && super.onOptionsItemSelected(item);
    }

    public void connectWebSocket() {
        URI uri = deriveURI();

        if (uri != null) {
            mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.i("Websocket", "Opened");
                }

                @Override
                public void onMessage(String s) {
                    // Log.i(TAG, s);
                    runOnUiThread(new MessageParser(s, new SafeCallback<MycroftUtterances>() {
                        @Override
                        public void call(@NonNull MycroftUtterances mu) {
                            addData(mu);
                        }
                    }));
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.i("Websocket", "Closed " + s);

                }

                @Override
                public void onError(Exception e) {
                    Log.i("Websocket", "Error " + e.getMessage());
                }
            };
            mWebSocketClient.connect();
        }
    }

    private void addData(MycroftUtterances mu) {
        utterances.add(mu);
        ma.notifyItemInserted(utterances.size() - 1);
        if (voxSwitch.isChecked()) {
            ttsManager.addQueue(mu.utterance);
        }
        recList.smoothScrollToPosition(ma.getItemCount() - 1);
    }

    private void registerReceivers() {
        registerNetworkReceiver();
        registerWearBroadcastReceiver();
    }

    private void registerNetworkReceiver(){
        if(!isNetworkChangeReceiverRegistered) {
            // set up the dynamic broadcast receiver for maintaining the socket
            networkChangeReceiver = new NetworkChangeReceiver();
            networkChangeReceiver.setMainActivityHandler(this);

            // set up the intent filters
            IntentFilter connChange = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            IntentFilter wifiChange = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
            registerReceiver(networkChangeReceiver, connChange);
            registerReceiver(networkChangeReceiver, wifiChange);

            isNetworkChangeReceiverRegistered = true;
        }
    }

    private void registerWearBroadcastReceiver() {
        if(!isWearBroadcastRevieverRegistered) {
            wearBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String message = intent.getStringExtra(Constants.MYCROFT_WEAR_REQUEST_MESSAGE);
                    // send to mycroft
                    if(message != null) {
                        Log.d(TAG, "Wear message received: [" + message +"] sending to Mycroft");
                        sendMessage(message);
                    }
                }
            };

            LocalBroadcastManager.getInstance(this).registerReceiver((wearBroadcastReceiver), new IntentFilter(Constants.MYCROFT_WEAR_REQUEST));
            isWearBroadcastRevieverRegistered = true;
        }
    }

    private void unregisterReceivers() {
        unregisterBroadcastReceiver(networkChangeReceiver);
        unregisterBroadcastReceiver(wearBroadcastReceiver);

        isNetworkChangeReceiverRegistered = false;
        isWearBroadcastRevieverRegistered = false;
    }

    private void unregisterBroadcastReceiver(BroadcastReceiver broadcastReceiver) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

	/**
     * This method will attach the correct path to the
     * {@link #wsip} hostname to allow for communication
     * with a Mycroft instance at that address.
     * <p>
     *     If {@link #wsip} cannot be used as a hostname
     *     in a {@link URI} (e.g. because it's null), then
     *     this method will return null.
     * </p>
     *
     * @return a valid uri, or null
     */
    @Nullable
    private URI deriveURI() {
        URI uri = null;

        if (wsip != null && !wsip.isEmpty()) {
            try {
                uri = new URI("ws://" + wsip + ":8181/core");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            uri = null;
        }
        return uri;
    }

    public void sendMessage(String msg) {
        // let's keep it simple eh?
        //final String json = "{\"message_type\":\"recognizer_loop:utterance\", \"context\": null, \"metadata\": {\"utterances\": [\"" + msg + "\"]}}";
        final String json = "{\"data\": {\"utterances\": [\"" + msg + "\"]}, \"type\": \"recognizer_loop:utterance\", \"context\": null}";

            try {
                if (mWebSocketClient == null || mWebSocketClient.getConnection().isClosed()) {
                    // try and reconnect
                    if (NetworkUtil.getConnectivityStatus(this) == NetworkUtil.NETWORK_STATUS_WIFI) { //TODO: add config to specify wifi only.
                        connectWebSocket();
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // Actions to do after 1 seconds
                        try {
                            mWebSocketClient.send(json);
                        } catch (WebsocketNotConnectedException exception) {
                            showToast(getResources().getString(R.string.websocket_closed));
                        }
                    }
                }, 1000);

            } catch (WebsocketNotConnectedException exception) {
                showToast(getResources().getString(R.string.websocket_closed));
            }
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            showToast(getString(R.string.speech_not_supported));
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    sendMessage(result.get(0));
                }
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
        isNetworkChangeReceiverRegistered = false;
        isWearBroadcastRevieverRegistered = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPreferences();
        recordVersionInfo();
        locationPermissionCheckAndSet();
        registerReceivers();
        checkIfLaunchedFromWidget(getIntent());
    }

    private void locationPermissionCheckAndSet() {
        try {
            SharedPreferences.Editor editor = sharedPref.edit();

            String valueToSet;

            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                valueToSet = "Set";

            } else {
                valueToSet = "Not Set";
            }
            editor.putString(LOCATION_PERMISSION_PREFERENCE_KEY, valueToSet);
           editor.apply();
        } catch (Exception ex){
            Log.d(TAG, ex.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterReceivers();

        if (launchedFromWidget) {
            autopromptForSpeech = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void loadPreferences(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // get mycroft-core ip address
        wsip = sharedPref.getString("ip", "");
        if (wsip.isEmpty()) {
            // eep, show the settings intent!
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (mWebSocketClient == null || mWebSocketClient.getConnection().isClosed()) {
            connectWebSocket();
        }

        // set app reader setting
        voxSwitch.setChecked(sharedPref.getBoolean("appReaderSwitch", true));

        // determine if app reader should be visible
        if (sharedPref.getBoolean("displayAppReaderSwitch", true)) {
            voxSwitch.setVisibility(View.VISIBLE);
        } else {
            voxSwitch.setVisibility(View.INVISIBLE);
        }

        maximumRetries = Integer.parseInt(sharedPref.getString("maximumRetries", "1"));
    }

    protected void checkIfLaunchedFromWidget(Intent intent) {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("launchedFromWidget")) {
                launchedFromWidget = extras.getBoolean("launchedFromWidget");
                autopromptForSpeech = extras.getBoolean("autopromptForSpeech");
            }

            if (extras.containsKey(Constants.MYCROFT_WEAR_REQUEST_KEY_NAME)) {
                Log.d(TAG, "checkIfLaunchedFromWidget - extras contain key:" + Constants.MYCROFT_WEAR_REQUEST_KEY_NAME);
                sendMessage(extras.getString(Constants.MYCROFT_WEAR_REQUEST_KEY_NAME));
                getIntent().removeExtra(Constants.MYCROFT_WEAR_REQUEST_KEY_NAME);

            }
        } else {
            Log.d(TAG, "checkIfLaunchedFromWidget - extras are null");
        }

        if (autopromptForSpeech) {
            promptSpeechInput();
            intent.putExtra("autopromptForSpeech", false);
        }
    }

    private void recordVersionInfo() {
        String versionName = "";
        int versionCode = -1;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(VERSION_CODE_PREFERENCE_KEY, versionCode);
            editor.putString(VERSION_NAME_PREFERENCE_KEY, versionName);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showToast(String message) {
        GuiUtilities.showToast(getApplicationContext(), message);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}