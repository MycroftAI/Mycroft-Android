package mycroft.ai;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

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
import mycroft.ai.utils.NetworkAutoDiscoveryUtil;
import mycroft.ai.utils.NetworkUtil;

import android.widget.CompoundButton.OnCheckedChangeListener;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "Mycroft";

    public static final String MYCROFT_QUERY_MESSAGE_PATH = "/mycroft_query";
    public static final String MYCROFT_WEAR_REQUEST ="mycroft.ai.wear.request";
    public static final String MYCROFT_WEAR_REQUEST_MESSAGE ="mycroft.ai.wear.request.message";

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
                    String message = intent.getStringExtra(MYCROFT_WEAR_REQUEST_MESSAGE);
                    // send to mycroft
                    if(message != null) {
                        Log.d(TAG, "Wear message received: [" + message +"] sending to Mycroft");
                        sendMessage(message);
                    }
                }
            };

            LocalBroadcastManager.getInstance(this).registerReceiver((wearBroadcastReceiver), new IntentFilter(MYCROFT_WEAR_REQUEST));
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
                uri = new URI("ws://" + wsip + ":8000/events/ws");
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
        final String json = "{\"message_type\":\"recognizer_loop:utterance\", \"context\": null, \"metadata\": {\"utterances\": [\"" + msg + "\"]}}";

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
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.websocket_closed), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 1000);

            } catch (WebsocketNotConnectedException exception) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.websocket_closed), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
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
                    // txtSpeechInput.setText(result.get(0));
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
        registerReceivers();
        checkIfLaunchedFromWidget(getIntent());
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

            if (extras.containsKey("MYCROFT_WEAR_REQUEST")) {
                Log.d(TAG, "checkIfLaunchedFromWidget - extras contain key: MYCROFT_WEAR_REQUEST");
                sendMessage(extras.getString("MYCROFT_WEAR_REQUEST"));
            }
        } else {
            Log.d(TAG, "checkIfLaunchedFromWidget - extras are null");
        }

        if (autopromptForSpeech) {
            promptSpeechInput();
            intent.putExtra("autopromptForSpeech", false);
        }
    }

}