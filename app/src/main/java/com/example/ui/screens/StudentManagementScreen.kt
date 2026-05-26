package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(viewModel: AttendanceViewModel) {
    val students by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceRecords.collectAsState()

    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val filterDept by viewModel.selectedDept.collectAsState()
    val filterSem by viewModel.selectedSem.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Student?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Student?>(null) }

    // Dropdown expanded states
    var deptDropdownExpanded by remember { mutableStateOf(false) }
    var semDropdownExpanded by remember { mutableStateOf(false) }

    // Filtered list
    val filteredStudents = students.filter { student ->
        val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) ||
                student.registerNumber.contains(searchQuery, ignoreCase = true)
        val matchesDept = student.department == filterDept
        val matchesSem = student.semester == filterSem
        matchesSearch && matchesDept && matchesSem
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Database Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Student Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            AdminBottomNavigation(viewModel = viewModel, currentActive = "STUDENTS")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Record")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.studentSearchQuery.value = it },
                placeholder = { Text("Search by name or register roll...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.studentSearchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("student_search_input")
            )

            // Filtering Blocks (Dept & Semester Selects)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dept Dropdown select button
                Box(modifier = Modifier.weight(1.3f)) {
                    OutlinedButton(
                        onClick = { deptDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = filterDept,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = deptDropdownExpanded,
                        onDismissRequest = { deptDropdownExpanded = false }
                    ) {
                        viewModel.availableDepartments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.selectedDept.value = dept
                                    deptDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Semester Dropdown select button
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { semDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = filterSem,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = semDropdownExpanded,
                        onDismissRequest = { semDropdownExpanded = false }
                    ) {
                        viewModel.availableSemesters.forEach { sem ->
                            DropdownMenuItem(
                                text = { Text(sem, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.selectedSem.value = sem
                                    semDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Results count
            Text(
                text = "${filteredStudents.size} student profiles found in selection",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (filteredStudents.isEmpty()) {
                // Render gorgeous zero-state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Search empty",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No students found in current department/semester filters",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            maxLines = 2
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStudents, key = { it.registerNumber }) { student ->
                        val metrics = viewModel.calculateStudentMetrics(student.registerNumber, student.totalSemesterHours, attendanceLogs)
                        
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar matching college profile
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.name.take(2).uppercase(Locale.getDefault()),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Roll ID: ${student.registerNumber} • Section: ${student.section}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }

                                    // Interactive Eligibility Color Badges (Green, Orange, Red)
                                    EligibilityStatusBadge(status = metrics.eligibilityStatus, pctValue = metrics.attendancePercentage)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Calculated detail brief block
                                    Column {
                                        Text(
                                            text = "Attendance State",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Present: ${metrics.presentHours} / Conducted: ${metrics.totalConductedHours} periods",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Action controllers: Edit / Delete profile row
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { showEditDialog = student }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Profile",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(onClick = { showDeleteConfirmDialog = student }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Profile",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD STUDENT DIALOG
    if (showAddDialog) {
        StudentAddEditDialog(
            title = "Register New Student Card",
            studentToEdit = null,
            filterDept = filterDept,
            filterSem = filterSem,
            onDismiss = { showAddDialog = false },
            onConfirm = { newStudent ->
                viewModel.addStudent(newStudent)
                showAddDialog = false
            }
        )
    }

    // EDIT STUDENT DIALOG
    showEditDialog?.let { currentStud ->
        StudentAddEditDialog(
            title = "Update Student Information",
            studentToEdit = currentStud,
            filterDept = filterDept,
            filterSem = filterSem,
            onDismiss = { showEditDialog = null },
            onConfirm = { updatedStud ->
                viewModel.updateStudent(updatedStud)
                showEditDialog = null
            }
        )
    }

    // DELETE STUDENT CONFIRM DIALOG
    showDeleteConfirmDialog?.let { stud ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Confirmation") },
            text = { Text("Are you absolutely certain you want to permanently delete student profile ${stud.name} (${stud.registerNumber})? This will wipe all historic periods attendance logs and cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(stud)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EligibilityStatusBadge(status: String, pctValue: Double) {
    val (bgColor, textColor, label) = when (status) {
        "ELIGIBLE" -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), "${String.format(Locale.getDefault(), "%.1f", pctValue)}% Eligible")
        "MEDICAL_REQUIRED" -> Triple(Color(0xFFFEF7E0), Color(0xFFB06000), "${String.format(Locale.getDefault(), "%.1f", pctValue)}% Medical")
        "CONTINUE_STUDY" -> Triple(Color(0xFFFCE8E6), Color(0xFFC5221F), "${String.format(Locale.getDefault(), "%.1f", pctValue)}% Study Cont.")
        else -> Triple(Color(0xFFFCE8E6), Color(0xFFC5221F), "${String.format(Locale.getDefault(), "%.1f", pctValue)}% Shortage")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun StudentAddEditDialog(
    title: String,
    studentToEdit: Student?,
    filterDept: String,
    filterSem: String,
    onDismiss: () -> Unit,
    onConfirm: (Student) -> Unit
) {
    var regNo by remember { mutableStateOf(studentToEdit?.registerNumber ?: "") }
    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var department by remember { mutableStateOf(studentToEdit?.department ?: filterDept) }
    var semester by remember { mutableStateOf(studentToEdit?.semester ?: filterSem) }
    var section by remember { mutableStateOf(studentToEdit?.section ?: "A") }
    var academicYear by remember { mutableStateOf(studentToEdit?.academicYear ?: "2024-2027") }
    var totalHoursStr by remember { mutableStateOf(studentToEdit?.totalSemesterHours?.toString() ?: "640") }

    val departments = listOf("CIVIL", "MECHANICAL", "EEE", "ECE", "COMPUTER")
    val semesters = listOf("Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8")

    var isDeptDropdown by remember { mutableStateOf(false) }
    var isSemDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reg number (Primary key, readable only on creation)
                OutlinedTextField(
                    value = regNo,
                    onValueChange = { regNo = it },
                    label = { Text("Register Number / Roll ID") },
                    singleLine = true,
                    enabled = studentToEdit == null,
                    modifier = Modifier.fillMaxWidth().testTag("reg_no_input")
                )

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("name_input")
                )

                // Department select
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        label = { Text("Department Branch") },
                        readOnly = true,
                        trailingIcon = { IconButton(onClick = { isDeptDropdown = true }) { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = isDeptDropdown, onDismissRequest = { isDeptDropdown = false }) {
                        departments.forEach { dept ->
                            DropdownMenuItem(text = { Text(dept) }, onClick = { department = dept; isDeptDropdown = false })
                        }
                    }
                }

                // Semester select
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = semester,
                        onValueChange = {},
                        label = { Text("Academic Semester") },
                        readOnly = true,
                        trailingIcon = { IconButton(onClick = { isSemDropdown = true }) { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = isSemDropdown, onDismissRequest = { isSemDropdown = false }) {
                        semesters.forEach { sem ->
                            DropdownMenuItem(text = { Text(sem) }, onClick = { semester = sem; isSemDropdown = false })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Section
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Academic Year
                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("Duration batch") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                // Semester hours total
                OutlinedTextField(
                    value = totalHoursStr,
                    onValueChange = { totalHoursStr = it },
                    label = { Text("Total Class Hours Conducted Target") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (regNo.isNotBlank() && name.isNotBlank()) {
                        val hours = totalHoursStr.toIntOrNull() ?: 640
                        val entity = Student(
                            registerNumber = regNo.trim(),
                            name = name.trim(),
                            department = department,
                            semester = semester,
                            section = section.trim().uppercase(Locale.getDefault()),
                            academicYear = academicYear.trim(),
                            totalSemesterHours = hours
                        )
                        onConfirm(entity)
                    }
                }
            ) {
                Text("Confirm & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
