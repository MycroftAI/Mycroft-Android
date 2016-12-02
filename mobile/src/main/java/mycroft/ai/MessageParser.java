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
        try {
            JSONObject obj = new JSONObject(message);
            String msgType = obj.optString("message_type");
            if (Objects.equals(msgType, "speak")) {
                String ret = obj.getJSONObject("metadata").getString("utterance");
                MycroftUtterances mu = new MycroftUtterances();
                mu.utterance = ret;
				callback.call(mu);
            }

        } catch (JSONException e) {
            Log.w(TAG, "The response received did not conform to our expected JSON formats.", e);
        }

    }
}
