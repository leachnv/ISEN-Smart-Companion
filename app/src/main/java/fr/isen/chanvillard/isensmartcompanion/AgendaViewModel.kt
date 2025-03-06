import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.chanvillard.isensmartcompanion.Course
import fr.isen.chanvillard.isensmartcompanion.Event

class AgendaViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("AgendaPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _courses = MutableLiveData<List<Course>>(loadCourses())
    val courses: LiveData<List<Course>> = _courses

    private val _selectedEvents = MutableLiveData<List<Event>>(loadEvents())
    val selectedEvents: LiveData<List<Event>> = _selectedEvents

    // Charger les cours depuis SharedPreferences
    private fun loadCourses(): List<Course> {
        val json = sharedPreferences.getString("courses", "[]")
        val type = object : TypeToken<List<Course>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Charger les événements depuis SharedPreferences
    private fun loadEvents(): List<Event> {
        val json = sharedPreferences.getString("events", "[]")
        val type = object : TypeToken<List<Event>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Sauvegarder les cours dans SharedPreferences
    private fun saveCourses(courses: List<Course>) {
        sharedPreferences.edit().putString("courses", gson.toJson(courses)).apply()
    }

    // Sauvegarder les événements dans SharedPreferences
    private fun saveEvents(events: List<Event>) {
        sharedPreferences.edit().putString("events", gson.toJson(events)).apply()
    }

    // Ajouter un cours et le sauvegarder
    fun addCourse(course: Course) {
        val updatedCourses = _courses.value.orEmpty() + course
        _courses.value = updatedCourses
        saveCourses(updatedCourses)
    }

    // Ajouter un événement à l'agenda et sauvegarder
    fun addEventToAgenda(event: Event) {
        val updatedEvents = _selectedEvents.value.orEmpty() + event
        _selectedEvents.value = updatedEvents
        saveEvents(updatedEvents)
    }

    // Supprimer un cours et sauvegarder
    fun removeCourse(course: Course) {
        val updatedCourses = _courses.value.orEmpty().filter { it.id != course.id }
        _courses.value = updatedCourses
        saveCourses(updatedCourses)
    }

    // Ajouter ou retirer un événement
    fun toggleEventSelection(event: Event) {
        val updatedEvents = if (_selectedEvents.value.orEmpty().contains(event)) {
            _selectedEvents.value.orEmpty().filter { it.id != event.id }
        } else {
            _selectedEvents.value.orEmpty() + event
        }
        _selectedEvents.value = updatedEvents
        saveEvents(updatedEvents)
    }
}
