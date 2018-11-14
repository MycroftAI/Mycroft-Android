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

package mycroft.ai

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch

import com.crashlytics.android.Crashlytics

import org.java_websocket.client.WebSocketClient
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ServerHandshake

import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Locale

import io.fabric.sdk.android.Fabric
import mycroft.ai.adapters.MycroftAdapter
import mycroft.ai.receivers.NetworkChangeReceiver
import mycroft.ai.shared.utilities.GuiUtilities
import mycroft.ai.shared.wear.Constants
import mycroft.ai.utils.NetworkUtil

import mycroft.ai.Constants.MycroftMobileConstants.LOCATION_PERMISSION_PREFERENCE_KEY
import mycroft.ai.Constants.MycroftMobileConstants.VERSION_CODE_PREFERENCE_KEY
import mycroft.ai.Constants.MycroftMobileConstants.VERSION_NAME_PREFERENCE_KEY
import mycroft.ai.shared.wear.Constants.MycroftSharedConstants.MYCROFT_WEAR_REQUEST
import mycroft.ai.shared.wear.Constants.MycroftSharedConstants.MYCROFT_WEAR_REQUEST_KEY_NAME
import mycroft.ai.shared.wear.Constants.MycroftSharedConstants.MYCROFT_WEAR_REQUEST_MESSAGE

class MainActivity : AppCompatActivity() {

    var mWebSocketClient: WebSocketClient?= null
    private var wsip: String? = null

    private var maximumRetries = 1

    private val REQ_CODE_SPEECH_INPUT = 100
    private var ttsManager: TTSManager? = null
    private var voxSwitch: Switch? = null

    private val utterances = ArrayList<MycroftUtterances>()

    private var ma = MycroftAdapter(utterances)

    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var wearBroadcastReceiver: BroadcastReceiver

    private var isNetworkChangeReceiverRegistered: Boolean = false
    private var isWearBroadcastRevieverRegistered: Boolean = false

    private lateinit var recList: RecyclerView

    private var sharedPref: SharedPreferences? = null

    private var launchedFromWidget = false
    private var autoPromptForSpeech = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { promptSpeechInput() }

        voxSwitch = findViewById(R.id.voxswitch) as Switch

        //attach a listener to check for changes in state
        voxSwitch!!.setOnCheckedChangeListener { buttonView, isChecked ->
            val editor = sharedPref!!.edit()
            editor.putBoolean("appReaderSwitch", isChecked)
            editor.commit()

            // stop tts from speaking if app reader disabled
            if (isChecked == false) {
                ttsManager!!.initQueue("")
            }
        }

        recList = findViewById(R.id.cardList) as RecyclerView
        recList.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.stackFromEnd = true
        llm.orientation = LinearLayoutManager.VERTICAL
        recList.layoutManager = llm

        recList.adapter = ma

        registerReceivers()

        ttsManager = TTSManager(this)


        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {


                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_COARSE_LOCATION)
            }
        }

        // start the discovery activity (testing only)
        // startActivity(new Intent(this, DiscoveryActivity.class));
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_setup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        var consumed = false
        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            consumed = true
        } else if (id == R.id.action_home_mycroft_ai) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://home.mycroft.ai"))
            startActivity(intent)
        } else if (id == R.id.action_beacons) {
            val intent = Intent(this, BeaconActivity::class.java)
            startActivity(intent)
            consumed = true
        }

        return consumed && super.onOptionsItemSelected(item)
    }

    fun connectWebSocket() {
        val uri = deriveURI()

        if (uri != null) {
            mWebSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(serverHandshake: ServerHandshake) {
                    Log.i("Websocket", "Opened")
                }

                override fun onMessage(s: String) {
                    // Log.i(TAG, s);
                    runOnUiThread(MessageParser(s, object : SafeCallback<MycroftUtterances> {
                        override fun call(mu: MycroftUtterances) {
                            addData(mu)
                        }
                    }))
                }

                override fun onClose(i: Int, s: String, b: Boolean) {
                    Log.i("Websocket", "Closed $s")

                }

                override fun onError(e: Exception) {
                    Log.i("Websocket", "Error " + e.message)
                }
            }
            mWebSocketClient!!.connect()
        }
    }

    private fun addData(mu: MycroftUtterances) {
        utterances.add(mu)
        ma.notifyItemInserted(utterances.size - 1)
        if (voxSwitch!!.isChecked) {
            ttsManager!!.addQueue(mu.utterance!!)
        }
        recList.smoothScrollToPosition(ma.itemCount - 1)
    }

    private fun registerReceivers() {
        registerNetworkReceiver()
        registerWearBroadcastReceiver()
    }

    private fun registerNetworkReceiver() {
        if (!isNetworkChangeReceiverRegistered) {
            // set up the dynamic broadcast receiver for maintaining the socket
            networkChangeReceiver = NetworkChangeReceiver()
            networkChangeReceiver.setMainActivityHandler(this)

            // set up the intent filters
            val connChange = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            val wifiChange = IntentFilter("android.net.wifi.WIFI_STATE_CHANGED")
            registerReceiver(networkChangeReceiver, connChange)
            registerReceiver(networkChangeReceiver, wifiChange)

            isNetworkChangeReceiverRegistered = true
        }
    }

    private fun registerWearBroadcastReceiver() {
        if (!isWearBroadcastRevieverRegistered) {
            wearBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val message = intent.getStringExtra(MYCROFT_WEAR_REQUEST_MESSAGE)
                    // send to mycroft
                    if (message != null) {
                        Log.d(TAG, "Wear message received: [$message] sending to Mycroft")
                        sendMessage(message)
                    }
                }
            }

            LocalBroadcastManager.getInstance(this).registerReceiver(wearBroadcastReceiver, IntentFilter(MYCROFT_WEAR_REQUEST))
            isWearBroadcastRevieverRegistered = true
        }
    }

    private fun unregisterReceivers() {
        unregisterBroadcastReceiver(networkChangeReceiver)
        unregisterBroadcastReceiver(wearBroadcastReceiver)

        isNetworkChangeReceiverRegistered = false
        isWearBroadcastRevieverRegistered = false
    }

    private fun unregisterBroadcastReceiver(broadcastReceiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    /**
     * This method will attach the correct path to the
     * [.wsip] hostname to allow for communication
     * with a Mycroft instance at that address.
     *
     *
     * If [.wsip] cannot be used as a hostname
     * in a [URI] (e.g. because it's null), then
     * this method will return null.
     *
     *
     * @return a valid uri, or null
     */
    private fun deriveURI(): URI? {
        var uri: URI? = null

        if (wsip != null && !wsip!!.isEmpty()) {
            try {
                uri = URI("ws://$wsip:8181/core")
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }

        } else {
            uri = null
        }
        return uri
    }

    fun sendMessage(msg: String?) {
        // let's keep it simple eh?
        //final String json = "{\"message_type\":\"recognizer_loop:utterance\", \"context\": null, \"metadata\": {\"utterances\": [\"" + msg + "\"]}}";
        val json = "{\"data\": {\"utterances\": [\"$msg\"]}, \"type\": \"recognizer_loop:utterance\", \"context\": null}"

        try {
            if (mWebSocketClient == null || mWebSocketClient!!.connection.isClosed) {
                // try and reconnect
                if (NetworkUtil.getConnectivityStatus(this) == NetworkUtil.NETWORK_STATUS_WIFI) { //TODO: add config to specify wifi only.
                    connectWebSocket()
                }
            }

            val handler = Handler()
            handler.postDelayed({
                // Actions to do after 1 seconds
                try {
                    mWebSocketClient!!.send(json)
                } catch (exception: WebsocketNotConnectedException) {
                    showToast(resources.getString(R.string.websocket_closed))
                }
            }, 1000)

        } catch (exception: WebsocketNotConnectedException) {
            showToast(resources.getString(R.string.websocket_closed))
        }

    }

    /**
     * Showing google speech input dialog
     */
    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt))
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            showToast(getString(R.string.speech_not_supported))
        }

    }

    /**
     * Receiving speech input
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && null != data) {

                    val result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                    sendMessage(result[0])
                }
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        ttsManager!!.shutDown()
        isNetworkChangeReceiverRegistered = false
        isWearBroadcastRevieverRegistered = false
    }

    public override fun onStart() {
        super.onStart()
        loadPreferences()
        recordVersionInfo()
        locationPermissionCheckAndSet()
        registerReceivers()
        checkIfLaunchedFromWidget(intent)
    }

    /**
     * For caching location permission state.
     */
    private fun locationPermissionCheckAndSet() {
        try {
            val editor = sharedPref!!.edit()

            val valueToSet: String

            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                valueToSet = "Set"

            } else {
                valueToSet = "Not Set"
            }
            editor.putString(LOCATION_PERMISSION_PREFERENCE_KEY, valueToSet)
            editor.apply()
        } catch (ex: Exception) {
            Log.d(TAG, ex.message)
        }

    }

    public override fun onStop() {
        super.onStop()

        unregisterReceivers()

        if (launchedFromWidget) {
            autoPromptForSpeech = true
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun loadPreferences() {
        //Load beacon prefs.
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // get mycroft-core ip address
        wsip = sharedPref!!.getString("ip", "")
        if (wsip!!.isEmpty()) {
            // eep, show the settings intent!
            startActivity(Intent(this, SettingsActivity::class.java))
        } else if (mWebSocketClient == null || mWebSocketClient!!.connection.isClosed) {
            connectWebSocket()
        }

        // set app reader setting
        voxSwitch!!.isChecked = sharedPref!!.getBoolean("appReaderSwitch", true)

        // determine if app reader should be visible
        if (sharedPref!!.getBoolean("displayAppReaderSwitch", true)) {
            voxSwitch!!.visibility = View.VISIBLE
        } else {
            voxSwitch!!.visibility = View.INVISIBLE
        }

        maximumRetries = Integer.parseInt(sharedPref!!.getString("maximumRetries", "1"))
    }

    private fun checkIfLaunchedFromWidget(intent: Intent) {

        val extras = getIntent().extras
        if (extras != null) {
            if (extras.containsKey("launchedFromWidget")) {
                launchedFromWidget = extras.getBoolean("launchedFromWidget")
                autoPromptForSpeech = extras.getBoolean("autoPromptForSpeech")
            }

            if (extras.containsKey(MYCROFT_WEAR_REQUEST_KEY_NAME)) {
                Log.d(TAG, "checkIfLaunchedFromWidget - extras contain key:" + MYCROFT_WEAR_REQUEST_KEY_NAME)
                sendMessage(extras.getString(MYCROFT_WEAR_REQUEST_KEY_NAME))
                getIntent().removeExtra(MYCROFT_WEAR_REQUEST_KEY_NAME)

            }
        } else {
            Log.d(TAG, "checkIfLaunchedFromWidget - extras are null")
        }

        if (autoPromptForSpeech) {
            promptSpeechInput()
            intent.putExtra("autoPromptForSpeech", false)
        }
    }

    private fun recordVersionInfo() {
        var versionName = ""
        var versionCode = -1
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionName = packageInfo.versionName
            versionCode = packageInfo.versionCode

            val editor = sharedPref!!.edit()
            editor.putInt(VERSION_CODE_PREFERENCE_KEY, versionCode)
            editor.putString(VERSION_NAME_PREFERENCE_KEY, versionName)
            editor.apply()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    private fun showToast(message: String) {
        GuiUtilities.showToast(applicationContext, message)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                //TODO look at why this is throwing an index out of bounds exception.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.location_result_title)
                    builder.setMessage(R.string.location_result_message)
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }

    companion object {

        private val TAG = "Mycroft"

        private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }
}
