package fr.isen.chanvillard.isensmartcompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Composant AgendaScreen qui affiche la liste des événements
@Composable
fun AgendaScreen() {
    // Utiliser un modèle simple pour afficher des événements factices
    val eventsList = listOf(
        Event("1", "Soirée BDE", "Une super soirée organisée par le BDE.", "2025-03-10", "Salle des fêtes", "Fête"),
        Event("2", "Gala ISEN", "Le gala annuel de l'ISEN.", "2025-06-20", "Palais des Congrès", "Cérémonie"),
        Event("3", "Journée Cohésion", "Une journée pour souder les étudiants.", "2025-04-15", "Campus ISEN", "Activité")
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Mon Agenda", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Affichage de la liste des événements avec LazyColumn
        LazyColumn {
            items(eventsList) { event ->
                EventItem(event = event)
            }
        }
    }
}

// Composant pour afficher chaque événement dans une Card
@Composable
fun EventItem(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp), // Bord arrondi pour la carte
        elevation = CardDefaults.cardElevation(4.dp) // Ajouter une légère élévation pour la carte
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Date: ${event.date}", fontSize = 16.sp)
            Text(text = "Lieu: ${event.location}", fontSize = 14.sp)
            Text(text = "Catégorie: ${event.category}", fontSize = 14.sp)
        }
    }
}
