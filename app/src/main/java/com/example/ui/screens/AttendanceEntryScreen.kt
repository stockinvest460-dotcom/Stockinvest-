package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceEntryScreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
    val attendanceGrid by viewModel.currentAttendanceGrid.collectAsState()

    val selectedDept by viewModel.selectedDept.collectAsState()
    val selectedSem by viewModel.selectedSem.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var isDeptDropdown by remember { mutableStateOf(false) }
    var isSemDropdown by remember { mutableStateOf(false) }
    var isSubjectDropdown by remember { mutableStateOf(false) }

    // Filter students for display
    val visibleStudents = students.filter {
        it.department == selectedDept && it.semester == selectedSem
    }

    // Trigger reload of grid when filter fields modify
    LaunchedEffect(selectedDept, selectedSem, selectedSubject, selectedDate) {
        viewModel.loadAttendanceGrid()
    }

    // Android Native Calendars Date Picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            viewModel.selectedDate.value = formattedDate
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Faculty Attendance Register", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    Button(
                        onClick = { viewModel.saveAttendanceGrid() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Sheet", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            AdminBottomNavigation(viewModel = viewModel, currentActive = "ATTENDANCE")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // FILTER AND SELECTION BOARD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Dept choice
                        Box(modifier = Modifier.weight(1.3f)) {
                            OutlinedButton(
                                onClick = { isDeptDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedDept, fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            DropdownMenu(expanded = isDeptDropdown, onDismissRequest = { isDeptDropdown = false }) {
                                viewModel.availableDepartments.forEach { dept ->
                                    DropdownMenuItem(text = { Text(dept, fontSize = 12.sp) }, onClick = { viewModel.selectedDept.value = dept; isDeptDropdown = false })
                                }
                            }
                        }

                        // Sem choice
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { isSemDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedSem, fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            DropdownMenu(expanded = isSemDropdown, onDismissRequest = { isSemDropdown = false }) {
                                viewModel.availableSemesters.forEach { sem ->
                                    DropdownMenuItem(text = { Text(sem, fontSize = 12.sp) }, onClick = { viewModel.selectedSem.value = sem; isSemDropdown = false })
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Subject Selection
                        Box(modifier = Modifier.weight(1.3f)) {
                            OutlinedButton(
                                onClick = { isSubjectDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedSubject, fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            DropdownMenu(expanded = isSubjectDropdown, onDismissRequest = { isSubjectDropdown = false }) {
                                viewModel.availableSubjects.forEach { sub ->
                                    DropdownMenuItem(text = { Text(sub, fontSize = 12.sp) }, onClick = { viewModel.selectedSubject.value = sub; isSubjectDropdown = false })
                                }
                            }
                        }

                        // Date Picker Action
                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedDate, fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        }
                    }

                    // Bulk selection utilities Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setAllVisibleStudentsBulk(present = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Present", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setAllVisibleStudentsBulk(present = false) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Absent", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // REGISTER HEADER LABELS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student List / Reg No",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1.2f)
                )

                Text(
                    text = "8-Periods Dynamic Grid",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.weight(2f),
                    textAlign = TextAlign.Center
                )
            }

            if (visibleStudents.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No students currently registered in $selectedDept • $selectedSem", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleStudents, key = { it.registerNumber }) { student ->
                        val periodsState = attendanceGrid[student.registerNumber] ?: BooleanArray(8) { true }
                        
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Student metadata info section
                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                        Text(student.registerNumber, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }

                                    // Quick toggle presets
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1.9f)
                                    ) {
                                        for (periodIdx in 0..7) {
                                            val isPresent = periodsState.getOrElse(periodIdx) { true }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isPresent) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isPresent) Color(0xFF137333).copy(alpha = 0.3f) else Color(0xFFC5221F).copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        viewModel.togglePeriodAttendance(student.registerNumber, periodIdx)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = "P${periodIdx + 1}",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isPresent) Color(0xFF137333) else Color(0xFFC5221F)
                                                    )
                                                    Icon(
                                                        imageVector = if (isPresent) Icons.Default.Check else Icons.Default.Close,
                                                        contentDescription = null,
                                                        tint = if (isPresent) Color(0xFF137333) else Color(0xFFC5221F),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Quick bulk rows for individual student
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val countPresent = periodsState.count { it }
                                    val countAbsent = 8 - countPresent

                                    Text(
                                        text = "$countPresent Present • $countAbsent Absent periods today",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Set All Present",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF137333),
                                            modifier = Modifier.clickable { viewModel.setBulkStudentAttendance(student.registerNumber, true) }
                                        )
                                        Text(
                                            text = "All Absent",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC5221F),
                                            modifier = Modifier.clickable { viewModel.setBulkStudentAttendance(student.registerNumber, false) }
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
