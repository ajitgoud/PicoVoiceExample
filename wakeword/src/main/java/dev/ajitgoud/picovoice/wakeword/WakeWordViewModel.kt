package dev.ajitgoud.picovoice.wakeword

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WakeWordViewModel @Inject constructor(private val wakeWordClient: WakeWordClient) :
    ViewModel() {
    val wakeWordState = wakeWordClient.wakeWordState

    fun init(context: Context, modelPath: String) {
        wakeWordClient.init(context, modelPath)
    }

    fun startWakeWord() {
        wakeWordClient.startWakeWord()
    }

    fun stopWakeWord() {
        wakeWordClient.stopWakeWord()
    }

    fun releaseWakeWordClient() {
        wakeWordClient.releaseResource()
    }

    override fun onCleared() {
        super.onCleared()
        releaseWakeWordClient()
    }

}