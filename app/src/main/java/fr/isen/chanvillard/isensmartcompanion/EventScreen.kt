package fr.isen.chanvillard.isensmartcompanion

import AgendaViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application

@Composable
fun EventsScreen() {
    val context = LocalContext.current // Utilisation correcte de LocalContext
    val agendaViewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(context.applicationContext as Application))

    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Nous n'avons plus besoin de la variable selectedEvents
    // val selectedEvents by agendaViewModel.selectedEvents.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        RetrofitInstance.retrofitService.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    events = response.body() ?: emptyList()
                } else {
                    errorMessage = "Échec du chargement des événements"
                }
                isLoading = false
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                errorMessage = "Erreur : ${t.localizedMessage}"
                isLoading = false
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Événements", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn {
                items(events) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                val gson = Gson()
                                val intent = Intent(context, EventDetailActivity::class.java).apply {
                                    putExtra("event_json", gson.toJson(event))
                                }
                                context.startActivity(intent) // 🔹 Utilisation correcte de startActivity
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray) // Simple couleur neutre
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(event.title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Text("Date: ${event.date}", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Nous supprimons le bouton d'ajout/retrait
                        }
                    }
                }
            }
        }
    }
}
