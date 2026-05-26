package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// Formats
private val dateFormatter = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault())

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AttendanceDatabase.getDatabase(application)
    private val repository = AttendanceRepository(db)

    // Current navigation screens
    enum class Screen {
        LOGIN,
        ADMIN_DASHBOARD,
        STAFF_PORTAL,
        STUDENT_PORTAL,
        STUDENT_MANAGEMENT,
        ATTENDANCE_ENTRY,
        REPORTS_MODULE,
        QR_SCANNER,
        NOTIFICATIONS_PANEL,
        FACULTY_MANAGEMENT,
        SUBJECT_MANAGEMENT
    }

    var currentScreen = MutableStateFlow(Screen.LOGIN)
        private set

    // Current user session details
    data class UserSession(
        val username: String,
        val role: String, // "ADMIN", "STAFF", "STUDENT"
        val registerNumber: String? = null // For student login
    )

    var userSession = MutableStateFlow<UserSession?>(null)
        private set

    // Saved login configurations (Remember me)
    var rememberMe = MutableStateFlow(false)
    var savedUsername = MutableStateFlow("")

    // DB state observers
    val students: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.allAttendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val faculties: StateFlow<List<Faculty>> = repository.allFaculty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjectsInDb: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter status for adding/taking attendance & viewing lists
    var selectedDept = MutableStateFlow("COMPUTER")
    var selectedSem = MutableStateFlow("Semester 5")
    var selectedSubject = MutableStateFlow("Data Structures")
    var selectedDate = MutableStateFlow("2026-05-26")

    // List of subjects
    val availableSubjects = listOf(
        "Data Structures",
        "Database Systems",
        "Software Eng",
        "Operating Systems",
        "Computer Networks",
        "Microprocessors"
    )

    // List of departments
    val availableDepartments = listOf(
        "CIVIL",
        "MECHANICAL",
        "EEE",
        "ECE",
        "COMPUTER"
    )

    // List of semesters
    val availableSemesters = listOf(
        "Semester 1",
        "Semester 2",
        "Semester 3",
        "Semester 4",
        "Semester 5",
        "Semester 6",
        "Semester 7",
        "Semester 8"
    )

    // Search query for student list
    var studentSearchQuery = MutableStateFlow("")

    // Temporary storage for attendance entry sheet (mapping register number to array of 8 periods)
    // Key: registerNumber, Value: Array of 8 Booleans (true = Present, false = Absent)
    var currentAttendanceGrid = MutableStateFlow<Map<String, BooleanArray>>(emptyMap())
        private set

    // Notifications Log
    data class NotificationItem(
        val id: String = UUID.randomUUID().toString(),
        val title: String,
        val body: String,
        val time: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
        val type: String // "danger", "warning", "info"
    )
    var notificationList = MutableStateFlow<List<NotificationItem>>(emptyList())
        private set

    // Initialize mock data if empty
    init {
        viewModelScope.launch {
            repository.seedMockDataIfEmpty()
            generateMockLogs()
        }
    }

    private fun generateMockLogs() {
        notificationList.value = listOf(
            NotificationItem(
                title = "Critical Attendance shortage Alert",
                body = "Student Pavel Novak (ME202601) attendance is below 50% limit.",
                type = "danger"
            ),
            NotificationItem(
                title = "Medical Certificate Requirement",
                body = "Student Elana Rostova (CS202603) is currently in orange eligibility category (68.0%).",
                type = "warning"
            ),
            NotificationItem(
                title = "Daily Attendance Synced Successfully",
                body = "Grid data for Semester 5 COMPUTER recorded to local offline storage.",
                type = "info"
            )
        )
    }

    // AUTH ACTIONS
    fun login(username: String, role: String, regNo: String? = null): Boolean {
        if (username.isBlank()) return false
        val resolvedRole = role.uppercase()
        userSession.value = UserSession(username, resolvedRole, regNo ?: username)
        
        if (rememberMe.value) {
            savedUsername.value = username
        }

        // Navigate to appropriate screen depending on role
        currentScreen.value = when (resolvedRole) {
            "ADMIN" -> Screen.ADMIN_DASHBOARD
            "STAFF" -> Screen.STAFF_PORTAL
            "STUDENT" -> Screen.STUDENT_PORTAL
            else -> Screen.LOGIN
        }

        triggerNotification(
            "Session Started",
            "Logged into Smart Attendance Pro as $resolvedRole ($username)",
            "info"
        )
        return true
    }

    fun logout() {
        userSession.value = null
        currentScreen.value = Screen.LOGIN
    }

    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
    }

    // STUDENT DATABASE CRUD
    fun addStudent(student: Student) {
        viewModelScope.launch {
            repository.addStudent(student)
            triggerNotification(
                "New Student Added",
                "Successfully added ${student.name} (${student.registerNumber}) to ${student.department}.",
                "info"
            )
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            triggerNotification(
                "Student Removed",
                "Student ${student.name} profile and all corresponding attendance logs have been permanently deleted.",
                "danger"
            )
        }
    }

    // FACULTY & SUBJECT DATABASE CRUD
    fun addFaculty(faculty: Faculty) {
        viewModelScope.launch {
            repository.addFaculty(faculty)
            triggerNotification(
                "New Faculty Added",
                "Successfully added ${faculty.name} (${faculty.facultyId}) as ${faculty.designation} under ${faculty.department}.",
                "info"
            )
        }
    }

    fun deleteFaculty(faculty: Faculty) {
        viewModelScope.launch {
            repository.deleteFaculty(faculty)
            triggerNotification(
                "Faculty Profile Deleted",
                "Successfully deleted faculty profile ${faculty.name}.",
                "danger"
            )
        }
    }

    fun addSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.addSubject(subject)
            triggerNotification(
                "New Subject Track Added",
                "Successfully added ${subject.name} [${subject.code}] for ${subject.department} ${subject.semester}.",
                "info"
            )
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            triggerNotification(
                "Subject Track Deleted",
                "Successfully deleted subject ${subject.name}.",
                "danger"
            )
        }
    }

    // ATTENDANCE ENTRY SHEET SYSTEM
    fun loadAttendanceGrid() {
        viewModelScope.launch {
            val currentStudents = students.value.filter {
                it.department == selectedDept.value && it.semester == selectedSem.value
            }
            
            // Fetch recorded records for the selected filters
            val logs = attendanceRecords.value.filter {
                it.date == selectedDate.value && it.subject == selectedSubject.value
            }

            val grid = mutableMapOf<String, BooleanArray>()
            for (student in currentStudents) {
                // Initialize default all periods present (true)
                val periods = BooleanArray(8) { true }
                
                // If there are logs in Db, load them
                val studentLogs = logs.filter { it.registerNumber == student.registerNumber }
                if (studentLogs.isNotEmpty()) {
                    for (log in studentLogs) {
                        if (log.period in 1..8) {
                            periods[log.period - 1] = log.isPresent
                        }
                    }
                }
                grid[student.registerNumber] = periods
            }
            currentAttendanceGrid.value = grid
        }
    }

    fun togglePeriodAttendance(registerNumber: String, periodIndex: Int) {
        val gridCopy = currentAttendanceGrid.value.toMutableMap()
        val periods = gridCopy[registerNumber]?.clone() ?: BooleanArray(8) { true }
        periods[periodIndex] = !periods[periodIndex]
        gridCopy[registerNumber] = periods
        currentAttendanceGrid.value = gridCopy
    }

    fun setBulkStudentAttendance(registerNumber: String, present: Boolean) {
        val gridCopy = currentAttendanceGrid.value.toMutableMap()
        gridCopy[registerNumber] = BooleanArray(8) { present }
        currentAttendanceGrid.value = gridCopy
    }

    fun setAllVisibleStudentsBulk(present: Boolean) {
        val gridCopy = currentAttendanceGrid.value.toMutableMap()
        for (registerNumber in gridCopy.keys) {
            gridCopy[registerNumber] = BooleanArray(8) { present }
        }
        currentAttendanceGrid.value = gridCopy
    }

    fun saveAttendanceGrid() {
        viewModelScope.launch {
            val recordsToInsert = mutableListOf<AttendanceRecord>()
            currentAttendanceGrid.value.forEach { (regNo, periods) ->
                periods.forEachIndexed { idx, isPresent ->
                    recordsToInsert.add(
                        AttendanceRecord(
                            registerNumber = regNo,
                            date = selectedDate.value,
                            period = idx + 1,
                            isPresent = isPresent,
                            subject = selectedSubject.value
                        )
                    )
                }
            }
            repository.saveAttendanceRecords(recordsToInsert)

            triggerNotification(
                "Attendance Records Saved",
                "Saved logs for ${recordsToInsert.size / 8} students on ${selectedDate.value} in ${selectedSubject.value}.",
                "info"
            )

            // Auto-check for low attendance warnings
            checkAttendanceStatisticsAndAlert()
        }
    }

    // NOTIFICATIONS SIMULATOR TRIGGER
    fun triggerNotification(title: String, body: String, type: String) {
        val item = NotificationItem(title = title, body = body, type = type)
        notificationList.value = listOf(item) + notificationList.value
    }

    private fun checkAttendanceStatisticsAndAlert() {
        val studentList = students.value
        val recordList = attendanceRecords.value

        for (student in studentList) {
            val stats = calculateStudentMetrics(student.registerNumber, student.totalSemesterHours, recordList)
            if (stats.attendancePercentage < 75.0 && stats.totalConductedHours > 30) {
                // If not already alerted
                val exists = notificationList.value.any { it.body.contains(student.name) && it.title.contains("Low Attendance Alert") }
                if (!exists) {
                    triggerNotification(
                        "Low Attendance Alert: ${student.name}",
                        "Student ${student.name} is at ${String.format(Locale.getDefault(), "%.1f", stats.attendancePercentage)}% attendance which is below eligibility.",
                        "warning"
                    )
                }
            }
        }
    }

    // LEAVE BALANCE PREDICTOR AND DETAILED STATISTICS MATHEMATICS
    data class StudentMetrics(
        val totalConductedHours: Int,
        val presentHours: Int,
        val absentHours: Int,
        val attendancePercentage: Double,
        val eligibilityStatus: String, // "ELIGIBLE", "MEDICAL_REQUIRED", "CONTINUE_STUDY", "NOT_ELIGIBLE"
        val maxSemesterLeavesAllowed: Int,
        val remainingSafeLeaveHours: Int,
        val remainingSafeLeaveDays: Double,
        val smartMessage: String,
        val hasAttendanceRisk: Boolean
    )

    fun calculateStudentMetrics(
        registerNumber: String,
        totalSemesterHours: Int = 640,
        logs: List<AttendanceRecord> = attendanceRecords.value
    ): StudentMetrics {
        val studentLogs = logs.filter { it.registerNumber == registerNumber }
        val totalConducted = studentLogs.size
        val present = studentLogs.count { it.isPresent }
        val absent = totalConducted - present

        val pct = if (totalConducted > 0) {
            (present.toDouble() / totalConducted.toDouble()) * 100.0
        } else {
            100.0 // Default perfect attendance on initial enroll
        }

        val eligibility = when {
            pct >= 75.0 -> "ELIGIBLE"
            pct >= 65.0 -> "MEDICAL_REQUIRED"
            pct >= 50.0 -> "CONTINUE_STUDY"
            else -> "NOT_ELIGIBLE"
        }

        // Leave predictions
        // Max leave hours allowed for 75% baseline across 640 total hours: 25% of 640 = 160 hours
        val maxSemLeaves = (0.25 * totalSemesterHours).toInt() // 160 hours
        val remainingSafeHours = maxSemLeaves - absent
        val remainingSafeDays = remainingSafeHours.toDouble() / 8.0 // assuming 8 periods = 1 day

        val hasRisk = pct < 75.0 || remainingSafeHours <= 16

        val message = when {
            pct < 50.0 -> "Critical: Not eligible. Attendance shortage is severe."
            pct < 65.0 -> "Attendance shortage started. Study Continue required."
            pct < 75.0 -> "Attendance shortage. Medical certificate will be required."
            remainingSafeHours <= 16 -> "Warning: Only ${String.format(Locale.getDefault(), "%.1f", remainingSafeDays)} safe leave days left!"
            else -> "Safe: You can still take ${String.format(Locale.getDefault(), "%.1f", remainingSafeDays)} leave days."
        }

        return StudentMetrics(
            totalConductedHours = totalConducted,
            presentHours = present,
            absentHours = absent,
            attendancePercentage = pct,
            eligibilityStatus = eligibility,
            maxSemesterLeavesAllowed = maxSemLeaves,
            remainingSafeLeaveHours = remainingSafeHours,
            remainingSafeLeaveDays = remainingSafeDays,
            smartMessage = message,
            hasAttendanceRisk = hasRisk
        )
    }

    // QR CODE SIMULATION PROCESS
    var qrScanningState = MutableStateFlow<String?>(null) // State message: e.g. success logged name
    fun simulateQRScan(registerNumber: String) {
        viewModelScope.launch {
            val student = students.value.find { it.registerNumber == registerNumber }
            if (student == null) {
                qrScanningState.value = "Error: Invalid student card QR code!"
                return@launch
            }

            // Record 8 periods of present for today
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val logs = (1..8).map { period ->
                AttendanceRecord(
                    registerNumber = student.registerNumber,
                    date = todayStr,
                    period = period,
                    isPresent = true,
                    subject = selectedSubject.value
                )
            }
            repository.saveAttendanceRecords(logs)
            qrScanningState.value = "Success! Attendance marked for ${student.name} ($registerNumber)"
            triggerNotification(
                "QR Attendance Logged",
                "Successfully verified student card. ${student.name} attendance captured for all 8 periods.",
                "info"
            )
        }
    }

    // EXPORT PDF AND EXCEL/CSV OPERATIONS WITH REAL SHARING
    fun generatePDFReport(context: Context) {
        try {
            val reportFile = File(context.cacheDir, "smart_attendance_report_${System.currentTimeMillis()}.pdf")
            val pdfDocument = PdfDocument()

            // Page info: A4 size is 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            paint.isAntiAlias = true

            // Title
            paint.textSize = 22f
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.DKGRAY
            canvas.drawText("SMART ATTENDANCE PRO", 40f, 60f, paint)

            // Subtitle
            paint.textSize = 12f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.GRAY
            canvas.drawText("Official Semester Attendance Eligibility Report • Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 40f, 85f, paint)

            paint.color = android.graphics.Color.LTGRAY
            canvas.drawLine(40f, 100f, 555f, 100f, paint)

            // Filter Meta Info
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText("Department: ${selectedDept.value}", 40f, 130f, paint)
            canvas.drawText("Semester: ${selectedSem.value}", 40f, 148f, paint)
            canvas.drawText("Subject Filter: ${selectedSubject.value}", 40f, 166f, paint)

            // Table headers
            paint.color = android.graphics.Color.parseColor("#1A73E8") // Blue
            canvas.drawRect(40f, 190f, 555f, 215f, paint)

            paint.color = android.graphics.Color.WHITE
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("Reg No", 48f, 207f, paint)
            canvas.drawText("Student Name", 140f, 207f, paint)
            canvas.drawText("Conducted", 320f, 207f, paint)
            canvas.drawText("Present", 390f, 207f, paint)
            canvas.drawText("Att %", 450f, 207f, paint)
            canvas.drawText("Status", 500f, 207f, paint)

            var currentY = 235f
            val recordList = attendanceRecords.value
            val visibleStudents = students.value.filter {
                it.department == selectedDept.value && it.semester == selectedSem.value
            }

            paint.color = android.graphics.Color.BLACK
            paint.isFakeBoldText = false

            for (student in visibleStudents) {
                // If we run out of page boundaries, wrap or stop
                if (currentY > 800f) break

                val metrics = calculateStudentMetrics(student.registerNumber, student.totalSemesterHours, recordList)

                canvas.drawText(student.registerNumber, 48f, currentY, paint)
                canvas.drawText(student.name, 140f, currentY, paint)
                canvas.drawText(metrics.totalConductedHours.toString(), 320f, currentY, paint)
                canvas.drawText(metrics.presentHours.toString(), 390f, currentY, paint)
                canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", metrics.attendancePercentage), 450f, currentY, paint)
                
                val statusShort = when (metrics.eligibilityStatus) {
                    "ELIGIBLE" -> "Eligible"
                    "MEDICAL_REQUIRED" -> "Medical"
                    "CONTINUE_STUDY" -> "Study Cont."
                    else -> "Low Attendance"
                }
                canvas.drawText(statusShort, 500f, currentY, paint)

                // Divider line
                paint.color = android.graphics.Color.parseColor("#F1F3F4")
                canvas.drawLine(40f, currentY + 6f, 555f, currentY + 6f, paint)
                paint.color = android.graphics.Color.BLACK

                currentY += 24f
            }

            pdfDocument.finishPage(page)

            val outputStream = FileOutputStream(reportFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            shareFile(context, reportFile, "application/pdf", "Smart_Attendance_Report.pdf")

        } catch (e: Exception) {
            Toast.makeText(context, "Error generating PDF report: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateExcelCSVReport(context: Context) {
        try {
            val reportFile = File(context.cacheDir, "smart_attendance_report_${System.currentTimeMillis()}.csv")
            val outputStream = FileOutputStream(reportFile)

            val stringBuffer = StringBuffer()
            // Header Info
            stringBuffer.append("Smart Attendance Pro - Attendance Report\n")
            stringBuffer.append("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
            stringBuffer.append("Department: ${selectedDept.value}, Semester: ${selectedSem.value}, Subject: ${selectedSubject.value}\n\n")

            // Table Header Columns
            stringBuffer.append("Register Number,Student Name,Department,Semester,Section,Conducted Hours,Present Hours,Absent Hours,Attendance Percentage,Eligibility Status\n")

            val recordList = attendanceRecords.value
            val visibleStudents = students.value.filter {
                it.department == selectedDept.value && it.semester == selectedSem.value
            }

            for (student in visibleStudents) {
                val metrics = calculateStudentMetrics(student.registerNumber, student.totalSemesterHours, recordList)
                stringBuffer.append(
                    "${student.registerNumber},\"${student.name}\",${student.department},${student.semester},${student.section}," +
                    "${metrics.totalConductedHours},${metrics.presentHours},${metrics.absentHours}," +
                    "${String.format(Locale.getDefault(), "%.2f", metrics.attendancePercentage)}%,${metrics.eligibilityStatus}\n"
                )
            }

            outputStream.write(stringBuffer.toString().toByteArray())
            outputStream.close()

            shareFile(context, reportFile, "text/csv", "Smart_Attendance_Report.csv")

        } catch (e: Exception) {
            Toast.makeText(context, "Error generating CSV report: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, displayTitle: String) {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Smart Attendance Pro Report - ${selectedDept.value}")
            putExtra(Intent.EXTRA_TEXT, "Detailed college attendance report of ${selectedDept.value} (${selectedSem.value}) is attached.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(intent, "Share Report via")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}
