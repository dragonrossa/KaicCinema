package hr.foi.air.cinema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import hr.foi.air.cinema.ui.screenings.ScreeningsPage
import hr.foi.air.cinema.ui.theme.CinemaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CinemaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScreeningsPage(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}