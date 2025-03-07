package fr.isen.chanvillard.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.chanvillard.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import retrofit2.Call
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ISENSmartCompanionTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavigationGraph(navController, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ✅ Interface Retrofit
interface EventApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf("home", "events", "history", "agenda")  // Ajoute "agenda" à la liste
    val icons = listOf(Icons.Filled.Home, Icons.Filled.CalendarToday, Icons.Filled.History, Icons.Filled.CalendarToday)
    val labels = listOf("Accueil", "Événements", "Historique", "Agenda")  // Label pour l'Agenda

    val currentRoute = navController.currentDestination?.route

    NavigationBar {
        items.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = labels[index]) },
                label = { Text(labels[index]) },
                selected = currentRoute == screen,
                onClick = {
                    navController.navigate(screen) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                    }
                }
            )
        }
    }
}



@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier) {
    NavHost(navController, startDestination = "home", modifier = modifier) {
        composable("home") { MainScreen() }
        composable("events") { EventsScreen() }
        composable("history") { HistoryScreen() }
        composable("agenda") { AgendaScreen() }  // Ajoute l'écran Agenda
    }
}



// ✅ Modèle d'événement
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String,
    var isSelected: Boolean = false) {
    fun getNumericId(): Int? {
        return id.toIntOrNull()
    }
}

// ✅ Liste factice d'événements
val fakeEvents = listOf(
    Event("1", "Soirée BDE", "Une super soirée organisée par le BDE.", "2025-03-10", "Salle des fêtes", "Fête"),
    Event("2", "Gala ISEN", "Le gala annuel de l'ISEN.", "2025-06-20", "Palais des Congrès", "Cérémonie"),
    Event("3", "Journée Cohésion", "Une journée pour souder les étudiants.", "2025-04-15", "Campus ISEN", "Activité"),
)


@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavigationBar(navController) }) {
        NavigationGraph(navController, Modifier.padding(it))
    }
}
