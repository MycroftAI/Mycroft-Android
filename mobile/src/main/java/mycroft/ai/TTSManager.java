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

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
     * External listener for error and success events. May be null.
     */
    @Nullable
    private TTSListener mTTSListener;

	/**
     * Create a new TTSManager attached to the given context.
     *
     * @param context any non-null context.
     */
    protected TTSManager(@NonNull Context context) {
        mTts = new TextToSpeech(context, onInitListener);
    }

	/**
	 * Special overload of constructor for testing purposes.
     *
     * @param textToSpeech the internal TTS this object will manage
     */
    protected TTSManager(@NonNull TextToSpeech textToSpeech) {
        mTts = textToSpeech;
    }

    /* package local */ TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTts.setLanguage(Locale.US);
                isLoaded = true;
                Log.i(TAG, "TTS initialized");

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    logError("This Language is not supported");
                }
            } else {
                logError("Initialization Failed!");
            }
        }
    };

    public void setTTSListener(TTSListener mTTSListener) {
        this.mTTSListener = mTTSListener;
    }

	/**
     * Wrapper for {@link TextToSpeech#shutdown()}
     */
    public void shutDown() {
        mTts.shutdown();
    }

    public void addQueue(String text) {
        if (isLoaded)
            mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
        else {
            logError("TTS Not Initialized");
        }
    }

    public void initQueue(String text) {

        if (isLoaded)
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        else
            logError("TTS Not Initialized");
    }

	/**
     * Wrapper around {@link Log#e(String, String)} that also notifies
     * the {@link #setTTSListener(TTSListener)}, if present.
     *
     * @param msg any non-null message
     */
    private void logError(@NonNull String msg) {
        if (mTTSListener != null) {
            mTTSListener.onError(msg);
        }
        Log.e(TAG, msg);
    }

    interface TTSListener {
        void onError(String message);
    }
}
