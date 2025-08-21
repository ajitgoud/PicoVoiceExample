package dev.ajitgoud.picovoice.wakeword

import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class WakeWordClient @Inject constructor() {

    private val _wakeWordState: MutableStateFlow<WakeWordState> =
        MutableStateFlow(WakeWordState.NotInitialized)
    val wakeWordState: StateFlow<WakeWordState> = _wakeWordState.asStateFlow()

    private var porcupineManager: PorcupineManager? = null
    var isListeningForWakeWord: Boolean = false
        private set

    private val wakeWordCallback = PorcupineManagerCallback { index ->
        if (index == 0) {
            _wakeWordState.tryEmit(WakeWordState.WakeWordDetected)
        }
    }

    fun init(context: Context, modelPath: String) {
        _wakeWordState.tryEmit(WakeWordState.Initializing)
        val builder = PorcupineManager.Builder()
            .setAccessKey(BuildConfig.PICOVOICE_ACCESS_KEY)
            .setSensitivity(0.8F)
            .setKeywordPaths(arrayOf(modelPath))

        try {
            porcupineManager = builder.build(context, wakeWordCallback)
            porcupineManager?.let {
                _wakeWordState.tryEmit(WakeWordState.Initialized)
            } ?: run {
                _wakeWordState.tryEmit(WakeWordState.Error("Failed to initialize!"))
            }
        } catch (e: Exception) {
            _wakeWordState.tryEmit(WakeWordState.Error("Failed to initialize! - ${e.message}"))
        }
    }

    fun startWakeWord() {
        porcupineManager?.let {
            if (!isListeningForWakeWord) {
                isListeningForWakeWord = true
                it.start()
            } else {
                _wakeWordState.tryEmit(WakeWordState.Error("Already listening for Wake word"))
            }
        } ?: run {
            _wakeWordState.tryEmit(WakeWordState.Error("Wake word is not initialized"))
        }
    }

    fun stopWakeWord() {
        porcupineManager?.let {
            if (isListeningForWakeWord) {
                isListeningForWakeWord = false
                it.stop()
            } else {
                _wakeWordState.tryEmit(WakeWordState.Error("Already not listening for Wake word"))
            }
        } ?: run {
            _wakeWordState.tryEmit(WakeWordState.Error("Wake word is not initialized"))
        }
    }

    fun releaseResource() {
        porcupineManager?.let {
            it.stop()
            it.delete()
            _wakeWordState.tryEmit(WakeWordState.NotInitialized)
        } ?: run {
            _wakeWordState.tryEmit(WakeWordState.Error("Wake word is not initialized"))
        }
        porcupineManager = null
    }

    sealed interface WakeWordState {
        data object NotInitialized : WakeWordState
        data object Initializing : WakeWordState
        data object Initialized : WakeWordState
        data object WakeWordDetected : WakeWordState
        data class Error(val message: String) : WakeWordState
    }
}