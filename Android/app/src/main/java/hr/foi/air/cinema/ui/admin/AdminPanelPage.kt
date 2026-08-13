package hr.foi.air.cinema.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val ADMIN_FEATURES = listOf(
    "Upravljanje projekcijama",
    "Objava novosti",
    "Odobravanje zahtjeva za rezervaciju",
)

@Composable
fun AdminPanelPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Admin panel", style = MaterialTheme.typography.headlineMedium)
        ADMIN_FEATURES.forEach { feature ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
