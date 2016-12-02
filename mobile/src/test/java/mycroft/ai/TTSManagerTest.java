package mycroft.ai;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Philip Cohn-Cort
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class TTSManagerTest {

	@Before
	public void setUp() throws Exception {
		mockStatic(Log.class);

		LogAnswer stdOut = new LogAnswer(System.out);
		when(Log.v(anyString(), anyString())).then(stdOut);
		when(Log.d(anyString(), anyString())).then(stdOut);
		when(Log.i(anyString(), anyString())).then(stdOut);

		LogAnswer stdErr = new LogAnswer(System.err);
		when(Log.w(anyString(), anyString())).then(stdErr);
		when(Log.e(anyString(), anyString())).then(stdErr);
		when(Log.wtf(anyString(), anyString())).then(stdErr);
	}

	@Test
	public void testAddingToQueueBeforeIsLoaded() throws Exception {
		TextToSpeech mock = mock(TextToSpeech.class);
		TTSManager ttsManager = new TTSManager(mock);

		TTSManager.TTSListener mockListener = mock(TTSManager.TTSListener.class);
		ttsManager.setTTSListener(mockListener);

		ttsManager.addQueue("text a");

		verify(mockListener).onError("TTS Not Initialized");
	}

	@Test
	public void testAddingToQueueAfterIsLoaded() throws Exception {
		TextToSpeech tts = mock(TextToSpeech.class);
		when(tts.setLanguage(any(Locale.class))).thenReturn(TextToSpeech.LANG_AVAILABLE);
		
		TTSManager ttsManager = new TTSManager(tts);

		TTSManager.TTSListener mockListener = mock(TTSManager.TTSListener.class);
		ttsManager.setTTSListener(mockListener);

		ttsManager.onInitListener.onInit(TextToSpeech.SUCCESS);

		verify(mockListener, never()).onError(anyString());
		
		ttsManager.addQueue("text a");

		verify(mockListener, never()).onError(anyString());
	}

	@Test
	public void testAddingToQueueTriggersSpeak() throws Exception {
		TextToSpeech tts = mock(TextToSpeech.class);
		when(tts.setLanguage(any(Locale.class))).thenReturn(TextToSpeech.LANG_AVAILABLE);

		TTSManager ttsManager = new TTSManager(tts);

		TTSManager.TTSListener mockListener = mock(TTSManager.TTSListener.class);
		ttsManager.setTTSListener(mockListener);

		ttsManager.onInitListener.onInit(TextToSpeech.SUCCESS);

		ttsManager.addQueue("text a");

		// Make sure that one of the speak methods was called, but we don't care which

		ArgumentCaptor<Integer> paramCaptor1 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> paramCaptor2 = ArgumentCaptor.forClass(Integer.class);

		//noinspection unchecked
		verify(tts, atLeast(0)).speak(anyString(), paramCaptor1.capture(), any(HashMap.class));
		verify(tts, atLeast(0)).speak(any(CharSequence.class), paramCaptor2.capture(), any(Bundle.class), anyString());

		assertTrue("One of the speak methods should have been called.",
				!paramCaptor1.getAllValues().isEmpty()
						||
						!paramCaptor2.getAllValues().isEmpty()
		);
	}

	@Test
	public void testFailureDoesNotSetIsLoaded() throws Exception {
		TextToSpeech tts = mock(TextToSpeech.class);
		when(tts.setLanguage(any(Locale.class))).thenReturn(TextToSpeech.LANG_AVAILABLE);

		TTSManager ttsManager = new TTSManager(tts);

		TTSManager.TTSListener mockListener = mock(TTSManager.TTSListener.class);
		ttsManager.setTTSListener(mockListener);

		ttsManager.onInitListener.onInit(TextToSpeech.ERROR);

		ttsManager.addQueue("text a");

		verify(mockListener, times(2)).onError(anyString());
	}
}