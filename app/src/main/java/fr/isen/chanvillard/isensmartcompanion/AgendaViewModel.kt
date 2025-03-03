package fr.isen.chanvillard.isensmartcompanion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AgendaViewModel(application: Application) : AndroidViewModel(application) {
    private val courseDao = CourseDatabase.getDatabase(application).courseDao()

    private val _courses = MutableLiveData<List<Course>>(emptyList())
    val courses: LiveData<List<Course>> = _courses

    private val _selectedEvents = MutableLiveData<List<Event>>(emptyList())
    val selectedEvents: LiveData<List<Event>> = _selectedEvents

    init {
        loadCourses()
    }

    private fun loadCourses() {
        // Coroutine pour charger les cours depuis la base de données
        viewModelScope.launch {
            _courses.value = courseDao.getAllCourses()
        }
    }

    fun addCourse(course: Course) {
        viewModelScope.launch {
            courseDao.insertCourse(course)
            loadCourses()  // Recharger la liste après l'ajout
        }
    }

    fun removeCourse(course: Course) {
        viewModelScope.launch {
            courseDao.deleteCourse(course)
            loadCourses()  // Recharger la liste après la suppression
        }
    }

    fun toggleEventSelection(event: Event) {
        val currentList = _selectedEvents.value ?: emptyList()
        _selectedEvents.value = if (currentList.contains(event)) {
            currentList - event
        } else {
            currentList + event
        }
    }
}

// Factory pour instancier AgendaViewModel avec un contexte Application
class AgendaViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendaViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
