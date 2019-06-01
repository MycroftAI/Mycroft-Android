package mycroft.ai

import android.speech.tts.TextToSpeech
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class TTSManagerTest {
    private val someSpeech = "some speech"

    private lateinit var subject: TTSManager

    @RelaxedMockK
    private lateinit var textToSpeech: TextToSpeech

    @RelaxedMockK
    private lateinit var ttsListener: TTSManager.TTSListener

    @BeforeEach
    fun setUp() {
        subject = TTSManager(textToSpeech)
    }

    @Test
    fun testAddQueue_BeforeIsLoaded_CallsOnError() {
        subject.setTTSListener(ttsListener)
        subject.addQueue(someSpeech)

        verify(exactly = 1) { ttsListener.onError(eq(subject.notInitializedErrorMessage)) }
    }

    @Test
    fun testAddQueue_WhenLoaded_DoesNotCallOnError() {
        subject.onInitListener.onInit(TextToSpeech.SUCCESS)
        verify(exactly = 0) { ttsListener.onError(any()) }
        subject.addQueue(someSpeech)
        verify(exactly = 0) { ttsListener.onError(any()) }
    }

    @Test
    fun testAddQueue_WhenLoaded_TriggersSpeak() {
        subject.onInitListener.onInit(TextToSpeech.SUCCESS)
        subject.addQueue(someSpeech)
        verify(exactly = 1) { textToSpeech.speak(any(), any(), any()) }
    }

    @Test
    fun testInitFailure_DoesNotSetLoadedTrue() {
        subject.setTTSListener(ttsListener)
        subject.onInitListener.onInit(TextToSpeech.ERROR)
        subject.addQueue(someSpeech)
        verify(exactly = 1) { ttsListener.onError(eq(subject.initializationFailedErrorMessage)) }
        verify(exactly = 1) { ttsListener.onError(eq(subject.notInitializedErrorMessage)) }

    }
}