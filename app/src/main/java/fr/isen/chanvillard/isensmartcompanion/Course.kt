package fr.isen.chanvillard.isensmartcompanion

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: String,
    val title: String,
    val date: String,
    val location: String
)
