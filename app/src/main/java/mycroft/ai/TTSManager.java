package mycroft.ai;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Locale;


/**
 * TTSManager is a wrapper around the Android System's Text-To-Speech ('TTS')
 * API.
 * <p>
 *     All constructors in this class require a context reference.
 *     Make sure to clean up with {@link #shutDown()} when the context's
 *     {@link Activity#onDestroy()} or {@link Service#onDestroy()} method is called.
 * </p>
 *
 * @see TextToSpeech
 *
 * @author Paul Scott
 */

public class TTSManager {

    private static final String TAG = "TTSManager";

	/**
	 * Backing TTS for this instance. Should not (ever) be null.
     */
    @NonNull
    private TextToSpeech mTts;
	/**
     * Whether the TTS is available for use (i.e. loaded into memory)
     */
    private boolean isLoaded = false;

	/**
     * Create a new TTSManager attached to the given context.
     *
     * @param context any non-null context.
     */
    protected TTSManager(@NonNull Context context) {
        mTts = new TextToSpeech(context, onInitListener);
    }

    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTts.setLanguage(Locale.US);
                isLoaded = true;
                Log.i(TAG, "TTS initialized");

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "This Language is not supported");
                }
            } else {
                Log.e(TAG, "Initialization Failed!");
            }
        }
    };

	/**
     * Wrapper for {@link TextToSpeech#shutdown()}
     */
    public void shutDown() {
        mTts.shutdown();
    }

    public void addQueue(String text) {
        if (isLoaded)
            mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
        else
            Log.e(TAG, "TTS Not Initialized");
    }

    public void initQueue(String text) {

        if (isLoaded)
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        else
            Log.e(TAG, "TTS Not Initialized");
    }
}
