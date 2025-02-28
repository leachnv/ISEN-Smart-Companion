package fr.isen.chanvillard.isensmartcompanion

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch



class AgendaViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("agenda_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _selectedEvents = mutableStateListOf<Event>()
    val selectedEvents: List<Event> get() = _selectedEvents

    init {
        loadSelectedEvents()
    }

    fun toggleEventSelection(event: Event) {
        if (_selectedEvents.contains(event)) {
            _selectedEvents.remove(event)
        } else {
            _selectedEvents.add(event)
        }
        saveSelectedEvents()
    }

    private fun saveSelectedEvents() {
        viewModelScope.launch {
            val json = gson.toJson(_selectedEvents)
            sharedPreferences.edit().putString("selected_events", json).apply()
        }
    }

    private fun loadSelectedEvents() {
        viewModelScope.launch {
            val json = sharedPreferences.getString("selected_events", null)
            if (json != null) {
                val type = object : TypeToken<List<Event>>() {}.type
                val savedEvents: List<Event> = gson.fromJson(json, type)
                _selectedEvents.clear()
                _selectedEvents.addAll(savedEvents)
            }
        }
    }
}
