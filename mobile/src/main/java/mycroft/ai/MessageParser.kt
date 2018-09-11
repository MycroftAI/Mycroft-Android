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

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

import java.util.Objects

/**
 * Specialised Runnable that parses the [JSONObject] in [.message]
 * when run. If it contains a [MycroftUtterances] object, the callback
 * defined in [the constructor][.MessageParser] will
 * be [called][SafeCallback.call] with that object as a parameter.
 *
 *
 * TODO: Add error-aware callback for cases where the message is malformed.
 *
 *
 * @author Philip Cohn-Cort
 */
internal class MessageParser
/**
 * MessageParser is a simple mechanism for parsing a [JSONObject] out
 * of a String in a way conducive to scheduling.
 *
 * @param message any String. Must not be null
 * @param callback will be referenced if a [MycroftUtterances]'
 * [MycroftUtterances.utterance]
 * is found within the message. May not be null
 */
(private val message: String, private val callback: SafeCallback<MycroftUtterances>) : Runnable {

    override fun run() {
        Log.i(TAG, message)
        // new format
        // {"data": {"utterance": "There are only two hard problems in Computer Science: cache invalidation, naming things and off-by-one-errors."}, "type": "speak", "context": null}
        try {
            val obj = JSONObject(message)
            val msgType = obj.optString("type")
            if (msgType == "speak") {
                val ret = obj.getJSONObject("data").getString("utterance")
                val mu = MycroftUtterances()
                mu.utterance = ret
                callback.call(mu)
            }

        } catch (e: JSONException) {
            Log.w(TAG, "The response received did not conform to our expected JSON formats.", e)
        }

    }

    companion object {

        private val TAG = "MessageParser"
    }
}
