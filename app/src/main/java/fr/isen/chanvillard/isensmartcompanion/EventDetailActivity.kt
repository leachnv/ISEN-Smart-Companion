package fr.isen.chanvillard.isensmartcompanion

import AgendaViewModel
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import fr.isen.chanvillard.isensmartcompanion.ui.theme.ISENSmartCompanionTheme

class EventDetailActivity : ComponentActivity() {

    // Déclarer le ViewModel en utilisant l'activité comme scope
    private val agendaViewModel: AgendaViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()

        // Récupération de l'Event depuis l'Intent
        val jsonEvent = intent.getStringExtra("event_json")
        val event = Gson().fromJson(jsonEvent, Event::class.java)

        setContent {
            ISENSmartCompanionTheme {
                // Passer agendaViewModel à EventDetailScreen
                EventDetailScreen(event = event, agendaViewModel = agendaViewModel)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "EVENT_REMINDER",
                "Rappels d'événements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications pour les rappels d'événements" }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun EventDetailScreen(event: Event, agendaViewModel: AgendaViewModel) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE)
    val agendaPrefs = context.getSharedPreferences("AgendaPrefs", Context.MODE_PRIVATE)
    var isReminderSet by remember { mutableStateOf(prefs.getBoolean(event.id, false)) }

    val saveReminderPreference = { isEnabled: Boolean ->
        prefs.edit().putBoolean(event.id, isEnabled).apply()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { (context as? ComponentActivity)?.finish() },
            modifier = Modifier.padding(top = 16.dp)
        ) { Text("Retour aux événements") }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = event.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Date: ${event.date}", fontSize = 18.sp)
        Text(text = "Lieu: ${event.location}", fontSize = 18.sp)
        Text(text = "Catégorie: ${event.category}", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = event.description, fontSize = 16.sp)

        IconButton(onClick = {
            isReminderSet = !isReminderSet
            saveReminderPreference(isReminderSet)

            // Gestion de l'ajout/suppression de l'événement dans l'agenda
            val existingEvents = agendaPrefs.getStringSet("agenda_events", mutableSetOf()) ?: mutableSetOf()
            val updatedEvents = existingEvents.toMutableSet()

            if (isReminderSet) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    (context as? EventDetailActivity)?.requestNotificationPermission()
                }
                scheduleNotification(context, event)

                // Ajout de l'événement dans l'agenda
                updatedEvents.add(Gson().toJson(event))
                agendaViewModel.addEventToAgenda(event) // Ajoute l'événement à l'agenda
            } else {
                updatedEvents.remove(Gson().toJson(event)) // Suppression si rappel désactivé
            }

            agendaPrefs.edit().putStringSet("agenda_events", updatedEvents).apply()
        }) {
            Icon(
                imageVector = if (isReminderSet) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                contentDescription = "Set Reminder"
            )
        }
    }
}

fun scheduleNotification(context: Context, event: Event) {
    val workManager = WorkManager.getInstance(context)
    val data = workDataOf("event_title" to event.title, "event_id" to event.id)

    val notificationRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(10, TimeUnit.SECONDS)
        .setInputData(data)
        .build()

    workManager.enqueue(notificationRequest)
}

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val eventTitle = inputData.getString("event_title") ?: "Événement"
        val eventId = inputData.getString("event_id") ?: "0"
        sendNotification(eventTitle, eventId)
        return Result.success()
    }

    private fun sendNotification(eventTitle: String, eventId: String) {
        val context = applicationContext
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
                as NotificationManager

        val notification = NotificationCompat.Builder(context, "EVENT_REMINDER")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Rappel d'événement")
            .setContentText("N'oubliez pas : $eventTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(eventId.hashCode(), notification)
    }
}
