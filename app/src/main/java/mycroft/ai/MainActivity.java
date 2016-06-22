package mycroft.ai;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import io.fabric.sdk.android.Fabric;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import mycroft.ai.receivers.NetworkChangeReceiver;
import mycroft.ai.utils.NetworkUtil;

import static mycroft.ai.R.id.textSpeechInput;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Mycroft";
    public WebSocketClient mWebSocketClient;
    public static final String PREFS_NAME = "MycroftPrefs";
    private String wsip;

    private final int REQ_CODE_SPEECH_INPUT = 100;
    TTSManager ttsManager = null;
    private TextView txtSpeechInput;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private int status;

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
                //Snackbar.make(view, "Press to talk to Mycroft!", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                promptSpeechInput();
            }
        });

        // set up the dynamic broadcast receiver for maintaining the socket
        NetworkChangeReceiver receiver = new NetworkChangeReceiver();
        receiver.setMainActivityHandler(this);
        // set up the intent filters
        IntentFilter connChange = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        IntentFilter wifiChange = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(receiver, connChange);
        registerReceiver(receiver, wifiChange);

        // Restore preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        wsip = sharedPref.getString("ip", "");
        if (wsip.isEmpty()) {
            // eep, show the settings intent!
            startActivity(new Intent(this, SettingsActivity.class));
        } else {
            connectWebSocket();
        }

        ttsManager = new TTSManager(this);
        txtSpeechInput = (TextView) findViewById(textSpeechInput);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://" + wsip + ":8000/events/ws");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, message);
                        try {
                            JSONObject obj = new JSONObject(message);
                            Log.e(TAG, obj.toString());
                            String msgType =  obj.getString("message_type");
                            if(msgType.equals("speak")) {
                                String ret = obj.getJSONObject("metadata").getString("utterance");
                                txtSpeechInput.setText(ret);
                                ttsManager.initQueue(ret);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
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

    public void sendMessage(String msg) {
        // let's keep it simple eh?
        String json = "{\"message_type\":\"recognizer_loop:utterance\", \"context\": null, \"metadata\": {\"utterances\": [\"" + msg + "\"]}}";
        try {
            if(mWebSocketClient.getConnection().isClosed()) {
                // try and reconnect
                if (status == NetworkUtil.NETWORK_STATUS_WIFI) {
                    connectWebSocket();
                }
            }
            mWebSocketClient.send(json);
            txtSpeechInput.setText(msg);
        } catch(WebsocketNotConnectedException e) {
            // Log.e(TAG, e.getMessage());
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
                    txtSpeechInput.setText(result.get(0));
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
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Mycroft Home") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://mycroft.ai"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        connectWebSocket();
    }
}
