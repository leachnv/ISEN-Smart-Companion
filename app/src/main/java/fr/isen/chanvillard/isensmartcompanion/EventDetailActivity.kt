package fr.isen.chanvillard.isensmartcompanion

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
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission accordée, l'utilisateur pourra recevoir des notifications
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel() // Créer le canal de notification

        val jsonEvent = intent.getStringExtra("event_json") // Récupérer l'event en JSON
        val event = Gson().fromJson(jsonEvent, Event::class.java) // Convertir en objet Event

        setContent {
            ISENSmartCompanionTheme {
                EventDetailScreen(event, this)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "EVENT_REMINDER",
                "Rappels d'événements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les rappels d'événements"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun EventDetailScreen(event: Event, activity: EventDetailActivity) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE)
    var isReminderSet by remember { mutableStateOf(prefs.getBoolean(event.id, false)) }

    // Enregistrer les préférences
    val saveReminderPreference = { isEnabled: Boolean ->
        prefs.edit().putBoolean(event.id, isEnabled).apply()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { (context as? ComponentActivity)?.finish() }, // Ferme l'activity et revient à la liste
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

        // Bouton pour activer/désactiver le rappel
        IconButton(onClick = {
            isReminderSet = !isReminderSet
            saveReminderPreference(isReminderSet)

            if (isReminderSet) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.requestNotificationPermission()
                }
                scheduleNotification(context, event)
            }
        }) {
            Icon(
                imageVector = if (isReminderSet) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                contentDescription = "Set Reminder"
            )
        }
    }
}

// ✅ Planifier la notification après 10 secondes
fun scheduleNotification(context: Context, event: Event) {
    val workManager = WorkManager.getInstance(context)

    val data = workDataOf(
        "event_title" to event.title,
        "event_id" to event.id
    )

    val notificationRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(10, TimeUnit.SECONDS)
        .setInputData(data)
        .build()

    workManager.enqueue(notificationRequest)
}

// ✅ Worker pour gérer la notification
class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val eventTitle = inputData.getString("event_title") ?: "Événement"
        val eventId = inputData.getString("event_id") ?: "0" // Pas besoin de le convertir en entier si c'est déjà une chaîne
        sendNotification(eventTitle, eventId) // Utilise eventId comme chaîne
        return Result.success()
    }

    private fun sendNotification(eventTitle: String, eventId: String) { // eventId est maintenant une chaîne
        val context = applicationContext
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager

        val notification = NotificationCompat.Builder(context, "EVENT_REMINDER")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Rappel d'événement")
            .setContentText("N'oubliez pas : $eventTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Utilise eventId comme chaîne pour l'ID de notification
        notificationManager.notify(eventId.hashCode(), notification) // Utilise hashCode si eventId est une chaîne
    }
}

