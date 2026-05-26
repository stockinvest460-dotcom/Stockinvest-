package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AttendanceRepository(private val db: AttendanceDatabase) {
    private val dao = db.dao

    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    val allAttendanceRecords: Flow<List<AttendanceRecord>> = dao.getAllAttendance()
    val allFaculty: Flow<List<Faculty>> = dao.getAllFaculty()
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()

    suspend fun addStudent(student: Student) {
        dao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        dao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        dao.deleteStudent(student)
        dao.deleteAttendanceForStudent(student.registerNumber)
    }

    suspend fun saveAttendanceRecords(records: List<AttendanceRecord>) {
        dao.insertAttendance(records)
    }

    fun getAttendanceForDateAndSubject(date: String, subject: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceForDateAndSubject(date, subject)
    }

    suspend fun addFaculty(faculty: Faculty) {
        dao.insertFaculty(faculty)
    }

    suspend fun deleteFaculty(faculty: Faculty) {
        dao.deleteFaculty(faculty)
    }

    suspend fun addSubject(subject: SubjectEntity) {
        dao.insertSubject(subject)
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        dao.deleteSubject(subject)
    }

    suspend fun seedMockDataIfEmpty() {
        val currentStudents = allStudents.first()
        if (currentStudents.isNotEmpty()) return

        // Seed some faculty members
        val mockFaculty = listOf(
            Faculty("FAC101", "Dr. Robert Hill", "COMPUTER", "robert.hill@college.edu", "Professor"),
            Faculty("FAC102", "Prof. Sarah Jenkins", "COMPUTER", "sarah.j@college.edu", "Assistant Professor"),
            Faculty("FAC103", "Dr. James Carter", "CIVIL", "james.carter@college.edu", "Associate Professor"),
            Faculty("FAC104", "Mrs. Evelyn Vance", "ECE", "evelyn.v@college.edu", "Lecturer"),
            Faculty("FAC105", "Mr. David Miller", "EEE", "david.m@college.edu", "Senior Lecturer")
        )
        for (f in mockFaculty) {
            dao.insertFaculty(f)
        }

        // Seed some subjects
        val mockSubjectEntities = listOf(
            SubjectEntity("CS-501", "Data Structures", "COMPUTER", "Semester 5", "FAC101"),
            SubjectEntity("CS-502", "Database Systems", "COMPUTER", "Semester 5", "FAC102"),
            SubjectEntity("CS-503", "Software Eng", "COMPUTER", "Semester 5", "FAC102"),
            SubjectEntity("CE-501", "Civil Estimating", "CIVIL", "Semester 5", "FAC103"),
            SubjectEntity("EC-501", "Digital Processing", "ECE", "Semester 5", "FAC104"),
            SubjectEntity("EE-501", "Circuit Analysis", "EEE", "Semester 3", "FAC105")
        )
        for (sub in mockSubjectEntities) {
            dao.insertSubject(sub)
        }

        // 1. Seed Students
        val mockStudents = listOf(
            Student("CS202601", "Amara Chen", "COMPUTER", "Semester 5", "A", "2024-2027", 640),
            Student("CS202602", "Devon Patel", "COMPUTER", "Semester 5", "A", "2024-2027", 640),
            Student("CS202603", "Elana Rostova", "COMPUTER", "Semester 5", "A", "2024-2027", 640),
            Student("CS202604", "Farhan Qureshi", "COMPUTER", "Semester 5", "B", "2024-2027", 640),
            Student("CS202605", "Grace Hopperley", "COMPUTER", "Semester 5", "B", "2024-2027", 640),
            Student("CE202601", "Amir Khan", "CIVIL", "Semester 5", "A", "2024-2027", 640),
            Student("CE202602", "Bina Das", "CIVIL", "Semester 5", "A", "2024-2027", 640),
            Student("IT202601", "Kenji Sato", "ECE", "Semester 5", "A", "2024-2027", 640),
            Student("IT202602", "Liam Nealon", "ECE", "Semester 5", "A", "2024-2027", 640),
            Student("IT202603", "Mei-Ling Zhou", "ECE", "Semester 5", "A", "2024-2027", 640),
            Student("EE202601", "Nkosi Johnson", "EEE", "Semester 3", "A", "2025-2028", 640),
            Student("EE202602", "Olivia Martinez", "EEE", "Semester 3", "A", "2025-2028", 640),
            Student("ME202601", "Pavel Novak", "MECHANICAL", "Semester 1", "A", "2026-2029", 640),
            Student("ME202602", "Quintin Vance", "MECHANICAL", "Semester 1", "B", "2026-2029", 640)
        )

        for (student in mockStudents) {
            dao.insertStudent(student)
        }

        // 2. Seed mock attendance history for COMPUTER Semester 5 Subject "Data Structures"
        // for several days in May 2026 to generate rich, calculation-valid reports
        val dates = listOf("2026-05-20", "2026-05-21", "2026-05-22", "2026-05-23", "2026-05-24", "2026-05-25")
        val csStudents = mockStudents.filter { it.department == "COMPUTER" }
        val subjects = listOf("Data Structures", "Database Systems", "Software Eng")

        val seededRecords = mutableListOf<AttendanceRecord>()

        for (date in dates) {
            for (sub in subjects) {
                for (student in csStudents) {
                    // Seed 8 periods for each day
                    for (period in 1..8) {
                        // Generate deterministic high & low attendance percentages per student
                        val hashValue = (student.registerNumber.hashCode() + date.hashCode() + sub.hashCode() + period) % 100
                        val attendanceRatio = when (student.registerNumber) {
                            "CS202601" -> 90 // High attendance (~90% present)
                            "CS202602" -> 78 // Medium high (~78% present)
                            "CS202603" -> 68 // Borderline (~68% present)
                            "CS202604" -> 58 // Low critical (~58% present)
                            "CS202605" -> 42 // Extremely low alert (~42% present)
                            else -> 80
                        }
                        val isPresent = (hashValue + 100) % 100 < attendanceRatio
                        seededRecords.add(
                            AttendanceRecord(
                                registerNumber = student.registerNumber,
                                date = date,
                                period = period,
                                isPresent = isPresent,
                                subject = sub
                            )
                        )
                    }
                }
            }
        }
        dao.insertAttendance(seededRecords)
    }
}
