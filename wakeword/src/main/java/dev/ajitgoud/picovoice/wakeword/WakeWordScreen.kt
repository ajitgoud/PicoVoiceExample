package dev.ajitgoud.picovoice.wakeword

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WakeWordScreen(
    viewModel: WakeWordViewModel = hiltViewModel()
) {
    val state by viewModel.wakeWordState.collectAsState(initial = WakeWordClient.WakeWordState.NotInitialized)
    val context = LocalContext.current
    var isMicPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            isMicPermissionGranted = true
        } else {
            isMicPermissionGranted = false
        }
    }
    WakeWordScreen(
        state = state,
        hasMicPermission = isMicPermissionGranted,
        onRequestPermission = { launcher.launch(Manifest.permission.RECORD_AUDIO) },
        onInit = {
            viewModel.init(
                context = context,
                modelPath = "wake_word.ppn"
            )
        },
        onStart = { viewModel.startWakeWord() },
        onStop = { viewModel.stopWakeWord() }
    )
}

@Composable
private fun WakeWordScreen(
    state: WakeWordClient.WakeWordState,
    hasMicPermission: Boolean,
    onRequestPermission: () -> Unit,
    onInit: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (state) {
                is WakeWordClient.WakeWordState.NotInitialized -> "Not Initialized"
                is WakeWordClient.WakeWordState.Initializing -> "Initializing..."
                is WakeWordClient.WakeWordState.Initialized -> "Initialized"
                is WakeWordClient.WakeWordState.WakeWordDetected -> "Wake Word Detected!"
                is WakeWordClient.WakeWordState.Error -> "Error: ${state.message}"
            }
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onInit,
            enabled = hasMicPermission && (state is WakeWordClient.WakeWordState.NotInitialized || state is WakeWordClient.WakeWordState.Error)
        ) {
            Text("Init Wake Word")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onStart,
            enabled = state is WakeWordClient.WakeWordState.Initialized
        ) {
            Text("Start Wake Word")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onStop,
            enabled = state is WakeWordClient.WakeWordState.WakeWordDetected
                    || state is WakeWordClient.WakeWordState.Initialized
        ) {
            Text("Stop Wake Word")
        }

        if (!hasMicPermission) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text("Grant Microphone Permission")
            }
        }
    }
}