package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubjectEntity
import com.example.ui.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectManagementScreen(viewModel: AttendanceViewModel) {
    val subjects by viewModel.subjectsInDb.collectAsState()
    val faculties by viewModel.faculties.collectAsState()
    val students by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceRecords.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var searchVal by remember { mutableStateOf("") }
    
    // Key: subject code, Value: whether expanded to view enrolled students data
    var expandedSubjectCode by remember { mutableStateOf<String?>(null) }

    val filteredSubjects = subjects.filter {
        it.name.contains(searchVal, ignoreCase = true) ||
                it.code.contains(searchVal, ignoreCase = true) ||
                it.department.contains(searchVal, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Subject Tracks", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AttendanceViewModel.Screen.ADMIN_DASHBOARD) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_subject_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Subject")
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
            // Search Input
            OutlinedTextField(
                value = searchVal,
                onValueChange = { searchVal = it },
                placeholder = { Text("Search by subject name or code...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchVal.isNotEmpty()) {
                        IconButton(onClick = { searchVal = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (filteredSubjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No subject tracks found in db.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSubjects, key = { it.code }) { subject ->
                        val isExpanded = expandedSubjectCode == subject.code
                        val assignedFaculty = faculties.find { it.facultyId == subject.facultyId }
                        
                        // Select current subject students matching department and semester
                        val enrolledStudents = students.filter {
                            it.department == subject.department && it.semester == subject.semester
                        }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.03f) 
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isExpanded) CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                            ) else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedSubjectCode = if (isExpanded) null else subject.code
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Book,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subject.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Code: ${subject.code} • ${subject.semester}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteSubject(subject) },
                                        modifier = Modifier.testTag("delete_sub_${subject.code}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Department badge
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(subject.department, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                    )

                                    // Faculty assignment label badge
                                    SuggestionChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = "Faculty: ${assignedFaculty?.name ?: "Unassigned (FAC ID: ${subject.facultyId})"}",
                                                fontSize = 10.sp
                                            )
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    )
                                }

                                // Interactive Expand Indicator
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${enrolledStudents.size} enrolled students matching filter",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand Students Details",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Sub-panel of Student enrolled Subject metrics data
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "Enrolled Students Attendance Summary:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        if (enrolledStudents.isEmpty()) {
                                            Text(
                                                text = "No students currently enrolled in ${subject.department} ${subject.semester}.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            enrolledStudents.forEach { student ->
                                                val studentMetrics = viewModel.calculateStudentMetrics(
                                                    student.registerNumber,
                                                    student.totalSemesterHours,
                                                    attendanceLogs
                                                )

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = student.name,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = student.registerNumber,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    // Custom Badge indicators for eligibility
                                                    val badgeColor = when (studentMetrics.eligibilityStatus) {
                                                        "ELIGIBLE" -> Color(0xFF10B981)
                                                        "MEDICAL_REQUIRED" -> Color(0xFFF59E0B)
                                                        "CONTINUE_STUDY" -> Color(0xFFEF4444)
                                                        else -> Color(0xFFB91C1C)
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = String.format(
                                                                "%.1f%%",
                                                                studentMetrics.attendancePercentage
                                                            ),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = badgeColor,
                                                            modifier = Modifier.padding(end = 6.dp)
                                                        )

                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .background(badgeColor, CircleShape)
                                                        )
                                                    }
                                                }
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
    }

    if (showAddDialog) {
        var code by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var selectedDept by remember { mutableStateOf("COMPUTER") }
        var selectedSem by remember { mutableStateOf("Semester 5") }
        var selectedFacultyId by remember { mutableStateOf("") }

        var isDeptDropdown by remember { mutableStateOf(false) }
        var isSemDropdown by remember { mutableStateOf(false) }
        var isFacultyDropdown by remember { mutableStateOf(false) }

        // Setup a default initial assigned faculty if available
        LaunchedEffect(faculties) {
            if (faculties.isNotEmpty() && selectedFacultyId.isEmpty()) {
                selectedFacultyId = faculties.first().facultyId
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Register New Subject Track", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Subject Code") },
                        placeholder = { Text("e.g. CS-501") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("subject_code_input")
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subject Name") },
                        placeholder = { Text("e.g. Computer Networks") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("subject_name_input")
                    )

                    // Department dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedDept,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Department Branch") },
                            trailingIcon = {
                                IconButton(onClick = { isDeptDropdown = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = isDeptDropdown,
                            onDismissRequest = { isDeptDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            viewModel.availableDepartments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept) },
                                    onClick = {
                                        selectedDept = dept
                                        isDeptDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Semester dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedSem,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Semester") },
                            trailingIcon = {
                                IconButton(onClick = { isSemDropdown = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = isSemDropdown,
                            onDismissRequest = { isSemDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            viewModel.availableSemesters.forEach { sem ->
                                DropdownMenuItem(
                                    text = { Text(sem) },
                                    onClick = {
                                        selectedSem = sem
                                        isSemDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Faculty dropdown (read from local DB)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentFac = faculties.find { it.facultyId == selectedFacultyId }
                        val facultyLabelValue = if (currentFac != null) {
                            "${currentFac.name} (${currentFac.facultyId})"
                        } else {
                            if (selectedFacultyId.isEmpty()) "Select Assigned Faculty" else selectedFacultyId
                        }

                        OutlinedTextField(
                            value = facultyLabelValue,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assigned Faculty Staff") },
                            trailingIcon = {
                                IconButton(onClick = { isFacultyDropdown = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = isFacultyDropdown,
                            onDismissRequest = { isFacultyDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (faculties.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No Registered Faculty Found!") },
                                    onClick = { isFacultyDropdown = false }
                                )
                            } else {
                                faculties.forEach { fac ->
                                    DropdownMenuItem(
                                        text = { Text("${fac.name} (${fac.facultyId})") },
                                        onClick = {
                                            selectedFacultyId = fac.facultyId
                                            isFacultyDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank() && name.isNotBlank() && selectedFacultyId.isNotBlank()) {
                            viewModel.addSubject(
                                SubjectEntity(
                                    code = code,
                                    name = name,
                                    department = selectedDept,
                                    semester = selectedSem,
                                    facultyId = selectedFacultyId
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_subject_btn")
                ) {
                    Text("Register Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}
