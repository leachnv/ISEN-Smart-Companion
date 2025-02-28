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

data class Course(
    val id: String,
    val title: String,
    val date: String,
    val location: String
)


@Composable
fun AgendaScreen() {
    var coursesList by remember { mutableStateOf(listOf<Course>()) }
    var showDialog by remember { mutableStateOf(false) }

    val eventsList = listOf(
        Event("1", "Soirée BDE", "Une super soirée organisée par le BDE.", "2025-03-10", "Salle des fêtes", "Fête"),
        Event("2", "Gala ISEN", "Le gala annuel de l'ISEN.", "2025-06-20", "Palais des Congrès", "Cérémonie"),
        Event("3", "Journée Cohésion", "Une journée pour souder les étudiants.", "2025-04-15", "Campus ISEN", "Activité")
    )

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Trier événements et cours par date
    val sortedEvents = eventsList.sortedBy { dateFormat.parse(it.date) }
    val sortedCourses = coursesList.sortedBy { dateFormat.parse(it.date) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mon Agenda", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item { Text("Événements", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            items(sortedEvents) { EventItem(it) }

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
fun EventItem(event: Event) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Date: ${event.date}", fontSize = 16.sp)
            Text("Lieu: ${event.location}", fontSize = 14.sp)
            Text("Catégorie: ${event.category}", fontSize = 14.sp)
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
