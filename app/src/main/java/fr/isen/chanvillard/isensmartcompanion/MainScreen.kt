package fr.isen.chanvillard.isensmartcompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    AssistantUI()
}

@Composable
fun AssistantUI() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val messageDao = remember { db.messageDao() }

    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }

    // Charger l'historique au démarrage
    val messagesFlow = messageDao.getAllMessages()
    val conversationState by messagesFlow.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(text = "ISEN", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        Text(text = "Smart Companion", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(conversationState) { message ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "👤 ${message.question}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "🤖 ${message.answer}", color = Color.DarkGray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White, shape = MaterialTheme.shapes.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                placeholder = { Text("Posez une question...") }
            )

            IconButton(
                onClick = {
                    val question = userInput.text
                    if (question.isNotEmpty()) {
                        userInput = TextFieldValue("")
                        isLoading = true

                        // Gérer la réponse de Gemini AI
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = GeminiAI.generateResponse(question) // Utiliser ton API pour récupérer la réponse
                                val message = Message(
                                    question = question,
                                    answer = response,
                                    timestamp = System.currentTimeMillis() // Ajouter un timestamp
                                )
                                messageDao.insertMessage(message)
                            } catch (e: Exception) {
                                val errorMessage = "Erreur : ${e.localizedMessage}"
                                messageDao.insertMessage(
                                    Message(
                                        question = question,
                                        answer = errorMessage,
                                        timestamp = System.currentTimeMillis() // Ajouter un timestamp
                                    )
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.Red, shape = CircleShape)
            ) {
                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Envoyer", tint = Color.White)
            }
        }
    }
}
