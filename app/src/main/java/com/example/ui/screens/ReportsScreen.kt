package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceRecords.collectAsState()

    val selectedDept by viewModel.selectedDept.collectAsState()
    val selectedSem by viewModel.selectedSem.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()

    var activeReportTypeIndex by remember { mutableStateOf(0) } // 0: Student-wise, 1: Subject-wise, 2: Daily audit

    var isDeptDropdown by remember { mutableStateOf(false) }
    var isSemDropdown by remember { mutableStateOf(false) }

    val reportTypes = listOf("Student Eligibility", "Subject Matrix", "Daily Logs")

    // Filter students
    val filteredStudents = students.filter {
        it.department == selectedDept && it.semester == selectedSem
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College ERP Reports Generator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            AdminBottomNavigation(viewModel = viewModel, currentActive = "REPORTS")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Dropdowns row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dept Selector
                Box(modifier = Modifier.weight(1.3f)) {
                    OutlinedButton(
                        onClick = { isDeptDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedDept, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = isDeptDropdown, onDismissRequest = { isDeptDropdown = false }) {
                        viewModel.availableDepartments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.selectedDept.value = dept
                                    isDeptDropdown = false
                                }
                            )
                        }
                    }
                }

                // Sem Selector
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { isSemDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedSem, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = isSemDropdown, onDismissRequest = { isSemDropdown = false }) {
                        viewModel.availableSemesters.forEach { sem ->
                            DropdownMenuItem(
                                text = { Text(sem, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.selectedSem.value = sem
                                    isSemDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // REPORT SUBTYPE PILLED TOGGLES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reportTypes.forEachIndexed { idx, title ->
                    val isSelected = activeReportTypeIndex == idx
                    FilledTonalButton(
                        onClick = { activeReportTypeIndex = idx },
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isSelected) {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            // EXPORTS DOCK ACTIONS PANEL
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Downloadable Print & Share Format Sheets",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PDF button
                        Button(
                            onClick = { viewModel.generatePDFReport(context) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)), // Adobe Red
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Excel (CSV) button
                        Button(
                            onClick = { viewModel.generateExcelCSVReport(context) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)), // Sheets Green
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // TABULAR TABLE DATA CARD VIEW
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Attendance Records Roster Grid",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Compact header layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Student / Reg ID", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("Cond", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text("Pres", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text("Ratio %", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                        Text("Eligibility", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }

                    if (filteredStudents.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No students in criteria.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredStudents, key = { it.registerNumber }) { student ->
                                val metrics = viewModel.calculateStudentMetrics(student.registerNumber, student.totalSemesterHours, attendanceLogs)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(student.name, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1)
                                        Text(student.registerNumber, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }

                                    // Conducted
                                    Text(
                                        text = metrics.totalConductedHours.toString(),
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(0.5f),
                                        textAlign = TextAlign.Center
                                    )

                                    // Present
                                    Text(
                                        text = metrics.presentHours.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(0.5f),
                                        textAlign = TextAlign.Center
                                    )

                                    // Percentage
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f%%", metrics.attendancePercentage),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(0.8f),
                                        color = if (metrics.attendancePercentage >= 75f) Color(0xFF137333) else Color(0xFFC5221F),
                                        textAlign = TextAlign.Center
                                    )

                                    // Short Badge
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        val statusLabel = when (metrics.eligibilityStatus) {
                                            "ELIGIBLE" -> "Eligible"
                                            "MEDICAL_REQUIRED" -> "Medical"
                                            "CONTINUE_STUDY" -> "Study Cont."
                                            else -> "Shortage"
                                        }
                                        val badgeColor = when (metrics.eligibilityStatus) {
                                            "ELIGIBLE" -> Color(0xFF137333)
                                            "MEDICAL_REQUIRED" -> Color(0xFFB06000)
                                            else -> Color(0xFFC5221F)
                                        }

                                        Text(
                                            text = statusLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                            }
                        }
                    }
                }
            }
        }
    }
}
