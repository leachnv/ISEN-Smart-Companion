package fr.isen.chanvillard.isensmartcompanion

import android.app.Application
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.LiveData
import androidx.compose.runtime.livedata.observeAsState // Assure-toi que cette importation est présente

import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgendaScreen() {
    val context = LocalContext.current.applicationContext as Application
    val agendaViewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(context))

    // Observer les cours depuis la base de données
    val courses by agendaViewModel.courses.observeAsState(emptyList())
    val selectedEvents by agendaViewModel.selectedEvents.observeAsState(emptyList())

    var showDialog by remember { mutableStateOf(false) }

    val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
    )

    fun parseDate(dateString: String): Date? {
        for (format in dateFormats) {
            try {
                return format.parse(dateString)
            } catch (e: Exception) {
                // Ignore and try the next format
            }
        }
        return null
    }

    val sortedCourses = courses.sortedBy { course -> parseDate(course.date) } // Corrige ici avec la syntaxe explicite

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Mon Agenda", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text("Événements Sélectionnés", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            items(selectedEvents) { event ->
                EventItem(event, isSelected = true, onToggleSelection = {
                    agendaViewModel.toggleEventSelection(it)
                })
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text("Mes Cours", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            items(sortedCourses) { course ->
                CourseItem(course, onDelete = {
                    agendaViewModel.removeCourse(it)
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
                    agendaViewModel.addCourse(newCourse)
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
