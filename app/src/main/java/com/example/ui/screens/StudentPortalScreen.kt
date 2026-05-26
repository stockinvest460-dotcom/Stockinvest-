package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student
import com.example.ui.theme.SageGray
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPortalScreen(viewModel: AttendanceViewModel) {
    val students by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceRecords.collectAsState()
    val session by viewModel.userSession.collectAsState()

    // Find the logged student details
    val activeStudentReg = session?.registerNumber ?: "CS202601"
    val currentStudent = students.find { it.registerNumber == activeStudentReg } 
        ?: Student(activeStudentReg, "Generic Cadet", "COMPUTER", "Semester 5", "A", "2024-2027", 640)

    val metrics = viewModel.calculateStudentMetrics(currentStudent.registerNumber, currentStudent.totalSemesterHours, attendanceLogs)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student ERP Portal", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ExitToApp,
                            contentDescription = "Log out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Student Profile Identity Card element
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = currentStudent.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reg No: ${currentStudent.registerNumber} • ${currentStudent.department}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${currentStudent.semester} • Section: ${currentStudent.section} (${currentStudent.academicYear})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // CIRCULAR VISUAL RADAR CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Aggregated Attendance Ratio",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Custom circular arc speedometer
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            
                            // Draw grey backing ring
                            drawCircle(
                                color = Color(0xFFF1F3F4),
                                style = Stroke(width = strokeWidth)
                            )

                            // Find progress rate
                            val rate = (metrics.attendancePercentage / 100.0).toFloat().coerceIn(0f, 1f)
                            val arcColor = when {
                                metrics.attendancePercentage >= 75.0 -> Color(0xFF10B981) // Green
                                metrics.attendancePercentage >= 65.0 -> Color(0xFFF59E0B) // Orange
                                else -> Color(0xFFEF4444) // Red
                            }

                            drawArc(
                                color = arcColor,
                                startAngle = -90f,
                                sweepAngle = 360f * rate,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", metrics.attendancePercentage),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(2.dp))

                            EligibilityStatusBadge(status = metrics.eligibilityStatus, pctValue = metrics.attendancePercentage)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Brief sub-metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MiniMetricBlock("Conducted", "${metrics.totalConductedHours}h", Icons.Default.History)
                        MiniMetricBlock("Present", "${metrics.presentHours}h", Icons.Default.Check)
                        MiniMetricBlock("Absent", "${metrics.absentHours}h", Icons.Default.Close)
                    }
                }
            }

            // SMART LEAVE BALANCE PREDICTOR DOCK
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (metrics.hasAttendanceRisk) Color(0xFFFFF1F1) else Color(0xFFEDF7ED)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (metrics.hasAttendanceRisk) Color(0xFFFCA5A5) else Color(0xFFA3E635)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (metrics.hasAttendanceRisk) Icons.Default.Warning else Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = if (metrics.hasAttendanceRisk) Color(0xFFDC2626) else Color(0xFF15803D),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Smart Leave Balance Predictor",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (metrics.hasAttendanceRisk) Color(0xFF991B1B) else Color(0xFF166534)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "For total conducting target hours: ${currentStudent.totalSemesterHours}h in this semester:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Max Leave Allowed", fontSize = 11.sp, color = SageGray)
                            Text("${metrics.maxSemesterLeavesAllowed} Hours (20 Days)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Safe Leave Balance", fontSize = 11.sp, color = SageGray)
                            Text(
                                text = "${metrics.remainingSafeLeaveHours} Hours (${String.format(Locale.getDefault(), "%.1f", metrics.remainingSafeLeaveDays)} Days)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.remainingSafeLeaveHours <= 16) Color.Red else Color.Unspecified
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = if (metrics.hasAttendanceRisk) Color(0xFFFCA5A5).copy(alpha = 0.4f) else Color(0xFFA3E635).copy(alpha = 0.4f))

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = metrics.smartMessage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (metrics.hasAttendanceRisk) Color(0xFFB91C1C) else Color(0xFF15803D),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // SUBJECT-WISE ROSTER LOGS
            Text(
                text = "Subject-Wise Detailed Registers",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            viewModel.availableSubjects.forEach { subjectName ->
                val logsForSubject = attendanceLogs.filter { it.registerNumber == currentStudent.registerNumber && it.subject == subjectName }
                val subTotal = logsForSubject.size
                val subPresent = logsForSubject.count { it.isPresent }
                val subPct = if (subTotal > 0) (subPresent.toDouble() / subTotal) * 100 else 100.0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subjectName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Classes conducted: $subTotal, Attended: $subPresent hours",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            // Individual percentage
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", subPct),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (subPct >= 75.0) Color(0xFF137333) else Color(0xFFC5221F)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Linear Progress indicator representing rate
                        LinearProgressIndicator(
                            progress = { (subPct / 100f).toFloat() },
                            color = if (subPct >= 75.0) Color(0xFF10B981) else Color(0xFFEF4444),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniMetricBlock(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, sizeIndexSideChannel(12.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = SageGray)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

fun sizeIndexSideChannel(dpSize: androidx.compose.ui.unit.Dp): androidx.compose.ui.Modifier {
    return Modifier.size(dpSize)
}
