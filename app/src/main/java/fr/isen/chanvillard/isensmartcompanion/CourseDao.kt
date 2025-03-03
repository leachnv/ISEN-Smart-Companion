package fr.isen.chanvillard.isensmartcompanion

import androidx.room.*

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<Course>

    @Delete
    suspend fun deleteCourse(course: Course)
}
