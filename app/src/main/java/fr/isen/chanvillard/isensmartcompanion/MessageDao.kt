package fr.isen.chanvillard.isensmartcompanion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Interface DAO pour gérer l'accès à la base de données
@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM message_table ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>  // Utiliser Flow pour récupérer les messages de manière réactive

    @Query("DELETE FROM message_table WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Int)

    @Query("DELETE FROM message_table")
    suspend fun deleteAllMessages()
}
