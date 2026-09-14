package hr.foi.air.cinema.ui.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Timestamp
import hr.foi.air.cinema.ui.common.formatScreeningTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreeningPage(
    screeningId: String,
    onScreeningUpdated: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditScreeningViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EditScreeningViewModel(screeningId = screeningId) }
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Uredi projekciju") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag na popis projekcija")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (val state = uiState) {
                is EditScreeningUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is EditScreeningUiState.NotFound -> {
                    Text(
                        text = "Projekcija nije pronađena",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                is EditScreeningUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                is EditScreeningUiState.Loaded -> {
                    EditScreeningForm(
                        initialMovieTitle = state.screening.movieTitle,
                        initialDescription = state.screening.description,
                        initialCategory = state.screening.category,
                        initialTotalSeats = state.screening.totalSeats.toString(),
                        initialScreeningTimeMillis = state.screening.screeningTime.toDate().time,
                        updateState = updateState,
                        onSaveClick = viewModel::updateScreening,
                        onScreeningUpdated = onScreeningUpdated,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditScreeningForm(
    initialMovieTitle: String,
    initialDescription: String,
    initialCategory: String,
    initialTotalSeats: String,
    initialScreeningTimeMillis: Long,
    updateState: UpdateScreeningUiState,
    onSaveClick: (movieTitle: String, description: String, category: String, totalSeatsInput: String, screeningTime: Timestamp) -> Unit,
    onScreeningUpdated: () -> Unit,
) {
    val context = LocalContext.current
    var movieTitle by rememberSaveable { mutableStateOf(initialMovieTitle) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var category by rememberSaveable { mutableStateOf(initialCategory) }
    var totalSeats by rememberSaveable { mutableStateOf(initialTotalSeats) }
    var calendar by remember {
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialScreeningTimeMillis })
    }

    LaunchedEffect(updateState) {
        if (updateState is UpdateScreeningUiState.Success) {
            onScreeningUpdated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = movieTitle,
            onValueChange = { movieTitle = it },
            label = { Text("Naziv filma") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Opis") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Kategorija") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = totalSeats,
            onValueChange = { totalSeats = it },
            label = { Text("Broj sjedala") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Termin: ${formatScreeningTime(Timestamp(calendar.time))}",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar = (calendar.clone() as Calendar).apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                ).show()
            }) {
                Text("Odaberi datum")
            }

            OutlinedButton(onClick = {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar = (calendar.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true,
                ).show()
            }) {
                Text("Odaberi vrijeme")
            }
        }

        if (updateState is UpdateScreeningUiState.Error) {
            Text(
                text = updateState.message,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            onClick = {
                onSaveClick(movieTitle, description, category, totalSeats, Timestamp(calendar.time))
            },
            enabled = updateState !is UpdateScreeningUiState.InProgress,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (updateState is UpdateScreeningUiState.InProgress) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Spremi izmjene")
            }
        }
    }
}
