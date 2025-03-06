package fr.isen.chanvillard.isensmartcompanion

import AgendaViewModel
import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import java.util.UUID

@Composable
fun AgendaScreen() {
    val context = LocalContext.current.applicationContext as Application
    val agendaViewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(context))

    // Observer les LiveData pour affichage en temps réel
    val courses by agendaViewModel.courses.observeAsState(emptyList())
    val selectedEvents by agendaViewModel.selectedEvents.observeAsState(emptyList())

    val agendaPrefs = context.getSharedPreferences("AgendaPrefs", Context.MODE_PRIVATE)
    val savedEvents = remember { mutableStateListOf<Event>() }

    // Charger les événements enregistrés dans l'agenda
    LaunchedEffect(Unit) {
        val eventsJson = agendaPrefs.getStringSet("agenda_events", mutableSetOf()) ?: mutableSetOf()
        savedEvents.clear()
        eventsJson.forEach {
            val event = Gson().fromJson(it, Event::class.java)
            savedEvents.add(event)
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Mon Agenda", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item { Text("Événements Sélectionnés", fontSize = 20.sp, fontWeight = FontWeight.Bold) }

            items(selectedEvents) { event ->
                EventItem(event, isSelected = true, onToggleSelection = {
                    agendaViewModel.toggleEventSelection(it)  // Mise à jour des événements
                })
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { Text("Mes Cours", fontSize = 20.sp, fontWeight = FontWeight.Bold) }

            items(courses) { course ->
                CourseItem(course, onDelete = {
                    agendaViewModel.removeCourse(it) // Suppression et mise à jour des SharedPreferences
                })
            }
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Ajouter un Cours")
        }
    }

    if (showDialog) {
        AddCourseDialog(
            onDismiss = { showDialog = false },
            onSave = { title, date, location ->
                if (title.isNotBlank() && date.isNotBlank() && location.isNotBlank()) {
                    val newCourse = Course(UUID.randomUUID().toString(), title, date, location)
                    agendaViewModel.addCourse(newCourse)  // Ajout et sauvegarde immédiate
                }
                showDialog = false
            }
        )
    }
}


@Composable
fun EventItem(event: Event, isSelected: Boolean, onToggleSelection: (Event) -> Unit) {
    val context = LocalContext.current.applicationContext as Application
    val sharedPreferences = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)

    // Ici, on vérifie si l'événement est dans l'agenda et on le met à jour en fonction de la notification
    val isNotified = sharedPreferences.getBoolean(event.id, false) // Vérifier si l'événement a été notifié

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Date: ${event.date}", fontSize = 16.sp)
            Text("Lieu: ${event.location}", fontSize = 14.sp)
            Text("Catégorie: ${event.category}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Ici, l'action est déclenchée uniquement si l'événement est notifié
            if (isNotified) {
                Button(onClick = {
                    // Toggle notification status
                    sharedPreferences.edit().putBoolean(event.id, !isNotified).apply()
                    // Toggle sélection dans l'agenda
                    onToggleSelection(event)
                }) {
                    Text(if (isSelected) "Retirer de l'Agenda" else "Ajouter à l'Agenda")
                }
            }
        }
    }
}

@Composable
fun CourseItem(course: Course, onDelete: (Course) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(course.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Date: ${course.date}", fontSize = 16.sp)
            Text("Lieu: ${course.location}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onDelete(course) }) {
                Text("Supprimer")
            }
        }
    }
}

@Composable
fun AddCourseDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un Cours") },
        text = {
            Column {
                TextField(value = title, onValueChange = { title = it }, label = { Text("Titre") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = location, onValueChange = { location = it }, label = { Text("Lieu") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, date, location) }) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
