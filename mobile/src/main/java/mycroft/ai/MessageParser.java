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

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Specialised Runnable that parses the {@link JSONObject} in {@link #message}
 * when run. If it contains a {@link MycroftUtterances} object, the callback
 * defined in {@link #MessageParser(String, SafeCallback) the constructor} will
 * be {@link SafeCallback#call(Object) called} with that object as a parameter.
 * <p>
 *     TODO: Add error-aware callback for cases where the message is malformed.
 * </p>
 *
 * @author Philip Cohn-Cort
 */
class MessageParser implements Runnable {

    private static final String TAG = "MessageParser";

	@NonNull
	private final String message;
	@NonNull
	private final SafeCallback<MycroftUtterances> callback;

	/**
	 * MessageParser is a simple mechanism for parsing a {@link JSONObject} out
	 * of a String in a way conducive to scheduling.
	 *
	 * @param message any String. Must not be null
	 * @param callback will be referenced if a {@link MycroftUtterances}'
	 *                 {@link MycroftUtterances#utterance}
	 *                 is found within the message. May not be null
	 */
	public MessageParser(@NonNull String message, @NonNull SafeCallback<MycroftUtterances> callback) {
		this.message = message;
		this.callback = callback;
	}

	@Override
    public void run() {
        Log.i(TAG, message);
		// new format
		// {"data": {"utterance": "There are only two hard problems in Computer Science: cache invalidation, naming things and off-by-one-errors."}, "type": "speak", "context": null}
		try {
            JSONObject obj = new JSONObject(message);
            String msgType = obj.optString("type");
            if (Objects.equals(msgType, "speak")) {
                String ret = obj.getJSONObject("data").getString("utterance");
                MycroftUtterances mu = new MycroftUtterances();
                mu.utterance = ret;
				callback.call(mu);
            }

        } catch (JSONException e) {
            Log.w(TAG, "The response received did not conform to our expected JSON formats.", e);
        }

    }
}
