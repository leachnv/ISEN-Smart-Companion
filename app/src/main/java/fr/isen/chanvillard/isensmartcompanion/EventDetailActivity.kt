package fr.isen.chanvillard.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import fr.isen.chanvillard.isensmartcompanion.ui.theme.ISENSmartCompanionTheme

class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val jsonEvent = intent.getStringExtra("event_json") // Récupérer l'event en JSON
        val event = Gson().fromJson(jsonEvent, Event::class.java) // Convertir en objet Event

        setContent {
            ISENSmartCompanionTheme {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(
                        onClick = { finish() }, // Ferme l'activity et revient à la liste
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retour aux événements")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = event.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Date: ${event.date}", fontSize = 18.sp)
                    Text(text = "Lieu: ${event.location}", fontSize = 18.sp)
                    Text(text = "Catégorie: ${event.category}", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = event.description, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ISENSmartCompanionTheme {
        Greeting("Android")
    }
}