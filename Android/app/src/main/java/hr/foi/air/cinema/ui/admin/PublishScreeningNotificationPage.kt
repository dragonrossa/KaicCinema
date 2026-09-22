package hr.foi.air.cinema.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreeningNotificationPage(
    screeningId: String,
    onNotificationPublished: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PublishScreeningNotificationViewModel = viewModel(
        factory = viewModelFactory {
            initializer { PublishScreeningNotificationViewModel(screeningId = screeningId) }
        },
    ),
) {
    var message by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is PublishScreeningNotificationUiState.Success) {
            onNotificationPublished()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Objavi obavijest") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag na popis projekcija")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Obavijest će biti povezana s ovom projekcijom i poslana svim korisnicima.",
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Tekst obavijesti") },
                modifier = Modifier.fillMaxWidth(),
            )

            val state = uiState
            if (state is PublishScreeningNotificationUiState.Error) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = { viewModel.publishNotification(message) },
                enabled = state !is PublishScreeningNotificationUiState.InProgress,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state is PublishScreeningNotificationUiState.InProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Pošalji obavijest")
                }
            }
        }
    }
}
