package fr.isen.chanvillard.isensmartcompanion

import android.content.Context
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val messageDao = remember { db.messageDao() }

    // Collecter les messages à partir du Flow
    val messages by messageDao.getAllMessages().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Historique", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (messages.isEmpty()) {
            Text("Aucun historique disponible.")
        } else {
            LazyColumn {
                items(messages) { message ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                // Supprimer un message lorsqu'il est cliqué
                                CoroutineScope(Dispatchers.IO).launch {
                                    messageDao.deleteMessageById(message.id)
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD00000))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "👤 ${message.question}", fontWeight = FontWeight.Bold)
                            Text(text = "🤖 ${message.answer}", color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Supprimer tout l'historique
            CoroutineScope(Dispatchers.IO).launch {
                messageDao.deleteAllMessages()
            }
        }) {
            Text("Supprimer tout l'historique")
        }
    }
}
