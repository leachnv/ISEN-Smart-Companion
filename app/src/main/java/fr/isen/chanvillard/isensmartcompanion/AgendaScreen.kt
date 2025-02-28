package fr.isen.chanvillard.isensmartcompanion

import fr.isen.chanvillard.isensmartcompanion.Event
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.AndroidViewModel


data class Course(
    val id: String,
    val title: String,
    val date: String,
    val location: String
)

// AgendaViewModelFactory pour fournir l'instance de AgendaViewModel
class AgendaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendaViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AgendaScreen() {
    val context = LocalContext.current.applicationContext as Application
    val agendaViewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(context))

    var coursesList by remember { mutableStateOf(listOf<Course>()) }
    var showDialog by remember { mutableStateOf(false) }

    val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),   // Format "2025-03-10"
        SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)      // Format "24 septembre 2024"
    )

    // Fonction pour parser plusieurs formats
    fun parseDate(dateString: String): Date? {
        for (format in dateFormats) {
            try {
                return format.parse(dateString)
            } catch (e: Exception) {
                // Ignore and try the next format
            }
        }
        return null // Retourne null si aucun format ne correspond
    }

    val eventsList = listOf(
        Event("1", "Soirée BDE", "Une super soirée organisée par le BDE.", "2025-03-10", "Salle des fêtes", "Fête"),
        Event("2", "Gala ISEN", "Le gala annuel de l'ISEN.", "2025-06-20", "Palais des Congrès", "Cérémonie"),
        Event("3", "Journée Cohésion", "Une journée pour souder les étudiants.", "2025-04-15", "Campus ISEN", "Activité")
    )

    // Trier les cours et événements en fonction de la date parsée
    val sortedCourses = coursesList.sortedBy { parseDate(it.date) }
    val selectedEvents = agendaViewModel.selectedEvents.sortedBy { parseDate(it.date) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mon Agenda", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item { Text("Événements Sélectionnés", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            items(selectedEvents) { event ->
                EventItem(event, isSelected = true) { agendaViewModel.toggleEventSelection(event) }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Tous les Événements", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            items(eventsList) { event ->
                EventItem(event, isSelected = event in selectedEvents) { agendaViewModel.toggleEventSelection(event) }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Mes Cours", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            items(sortedCourses) { CourseItem(it) }
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
                    coursesList = coursesList + Course(UUID.randomUUID().toString(), title, date, location)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun EventItem(event: Event, isSelected: Boolean, onToggleSelection: (Event) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Date: ${event.date}", fontSize = 16.sp)
            Text("Lieu: ${event.location}", fontSize = 14.sp)
            Text("Catégorie: ${event.category}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onToggleSelection(event) }) {
                Text(if (isSelected) "Retirer de l'Agenda" else "Ajouter à l'Agenda")
            }
        }
    }
}

@Composable
fun CourseItem(course: Course) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(course.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Date: ${course.date}", fontSize = 16.sp)
            Text("Lieu: ${course.location}", fontSize = 14.sp)
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
