package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val registerNumber: String,
    val name: String,
    val department: String,
    val semester: String,
    val section: String,
    val academicYear: String,
    val totalSemesterHours: Int = 640
)

@Entity(
    tableName = "attendance_records",
    primaryKeys = ["registerNumber", "date", "period", "subject"]
)
data class AttendanceRecord(
    val registerNumber: String,
    val date: String, // ISO date format: YYYY-MM-DD
    val period: Int, // 1 to 8
    val isPresent: Boolean,
    val subject: String
)

@Entity(tableName = "faculty")
data class Faculty(
    @PrimaryKey val facultyId: String,
    val name: String,
    val department: String,
    val email: String,
    val designation: String
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val code: String,
    val name: String,
    val department: String,
    val semester: String,
    val facultyId: String
)

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Query("SELECT * FROM attendance_records")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND subject = :subject")
    fun getAttendanceForDateAndSubject(date: String, subject: String): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE registerNumber = :registerNumber")
    suspend fun deleteAttendanceForStudent(registerNumber: String)

    // Faculty
    @Query("SELECT * FROM faculty ORDER BY name ASC")
    fun getAllFaculty(): Flow<List<Faculty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculty(faculty: Faculty)

    @Delete
    suspend fun deleteFaculty(faculty: Faculty)

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)
}

@Database(entities = [Student::class, AttendanceRecord::class, Faculty::class, SubjectEntity::class], version = 2, exportSchema = false)
abstract class AttendanceDatabase : RoomDatabase() {
    abstract val dao: AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AttendanceDatabase? = null

        fun getDatabase(context: android.content.Context): AttendanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttendanceDatabase::class.java,
                    "smart_attendance_pro_v2_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
