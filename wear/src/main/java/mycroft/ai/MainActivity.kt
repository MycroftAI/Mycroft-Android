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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.BoxInsetLayout
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

import java.util.Locale

import mycroft.ai.shared.utilities.GuiUtilities
import mycroft.ai.shared.wear.Constants.MycroftSharedConstants.MYCROFT_QUERY_MESSAGE_PATH

class MainActivity : WearableActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private val REQ_CODE_SPEECH_INPUT = 100

    private var containerView: BoxInsetLayout? = null
    private var inputImageButton: ImageButton? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mNode: Node? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAmbientEnabled()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        containerView = findViewById(R.id.container)

        inputImageButton = findViewById(R.id.inputImageButton)
        inputImageButton!!.setOnClickListener {
            try {
                promptSpeechInput()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
        updateDisplay()
    }

    override fun onUpdateAmbient() {
        super.onUpdateAmbient()
        updateDisplay()
    }

    override fun onExitAmbient() {
        updateDisplay()
        super.onExitAmbient()
    }

    private fun updateDisplay() {
        if (isAmbient) {
            containerView!!.setBackgroundColor(Color.BLACK)
        } else {
            containerView!!.background = null
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
            Toast.makeText(applicationContext,
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show()
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

    override fun onConnected(bundle: Bundle?) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback { getConnectedNodesResult ->
            for (node in getConnectedNodesResult.nodes) {
                if (node != null && node.isNearby) {
                    mNode = node
                    showToast("Connected To " + node.displayName)
                    Log.d(WEARABLE_MAIN, "Connected to " + node.displayName)
                } else {
                    showToast("Not Connected")
                    Log.d(WEARABLE_MAIN, "NOT CONNECTED")
                }
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {
        showToast("Connection Suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        showToast("Connection Failed")
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient!!.disconnect()
    }

    fun sendMessage(message: String) {
        if (mNode != null && mGoogleApiClient != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode!!.id, MYCROFT_QUERY_MESSAGE_PATH, message.toByteArray()).setResultCallback { sendMessageResult ->
                if (!sendMessageResult.status.isSuccess) {
                    showToast("Message Failed")
                } else {
                    showToast("Message Sent")
                }
            }
        } else {
            showToast("Unable To Send Message")
        }
    }

    private fun showToast(message: String) {
        GuiUtilities.showToast(applicationContext, message)
    }

    companion object {

        private val WEARABLE_MAIN = "WearableMain"
    }
}
