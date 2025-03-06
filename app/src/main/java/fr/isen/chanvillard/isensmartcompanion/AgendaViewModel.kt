package fr.isen.chanvillard.isensmartcompanion

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AgendaViewModel(application: Application) : AndroidViewModel(application) {
    private val courseDao = CourseDatabase.getDatabase(application).courseDao()
    private val sharedPreferences = application.getSharedPreferences("AgendaPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _courses = MutableLiveData<List<Course>>(emptyList())
    val courses: LiveData<List<Course>> = _courses

    private val _selectedEvents = MutableLiveData<List<Event>>(emptyList()) // ✅ Fix ici
    val selectedEvents: LiveData<List<Event>> = _selectedEvents

    init {
        loadCourses()
        loadSelectedEvents()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            _courses.value = courseDao.getAllCourses()
        }
    }

    fun addCourse(course: Course) {
        viewModelScope.launch {
            courseDao.insertCourse(course)
            loadCourses()
        }
    }

    fun removeCourse(course: Course) {
        viewModelScope.launch {
            courseDao.deleteCourse(course)
            loadCourses()
        }
    }

    fun toggleEventSelection(event: Event) {
        val currentList = _selectedEvents.value.orEmpty().toMutableList()

        if (currentList.contains(event)) {
            currentList.remove(event)
        } else {
            currentList.add(event)
        }

        _selectedEvents.value = currentList.toList() // ✅ Fix ici : Assurer une liste immuable
        saveSelectedEvents()
    }

    private fun saveSelectedEvents() {
        sharedPreferences.edit().putString("selected_events", gson.toJson(_selectedEvents.value)).apply()
    }

    private fun loadSelectedEvents() {
        val json = sharedPreferences.getString("selected_events", null)
        val list = if (json != null) {
            val type = object : TypeToken<List<Event>>() {}.type
            gson.fromJson<List<Event>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
        _selectedEvents.value = list
    }
}

// Factory pour instancier AgendaViewModel avec un contexte Application
class AgendaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendaViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}