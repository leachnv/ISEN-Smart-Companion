package fr.isen.chanvillard.isensmartcompanion

import androidx.room.Entity
import androidx.room.PrimaryKey

// Définition de la classe Message comme une entité Room
@Entity(tableName = "message_table")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Génère automatiquement un ID unique
    val question: String,
    val answer: String,
    val timestamp: Long // Ajout d'un champ timestamp pour suivre la date
)
