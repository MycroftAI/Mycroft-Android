package mycroft.ai;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Philip Cohn-Cort
 */
public class TTSManagerTest {

	@Test
	public void testAddingToQueue() throws Exception {
		TextToSpeech mock = mock(TextToSpeech.class);
		TTSManager ttsManager = new TTSManager(mock);

		TTSManager.TTSListener mockListener = mock(TTSManager.TTSListener.class);
		ttsManager.setTTSListener(mockListener);

		ttsManager.addQueue("text a");

		verify(mockListener).onError("TTS Not Initialized");
	}

	@Test
	public void test() throws Exception {

	}
}