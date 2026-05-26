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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student
import com.example.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: AttendanceViewModel) {
    val students by viewModel.students.collectAsState()
    val attendanceLogs by viewModel.attendanceRecords.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    // Analyze stats
    val totalStudentsCount = students.size
    
    var eligibleCount = 0
    var medicalCount = 0
    var continueStudyCount = 0
    var shortageCount = 0
    var totalPctSum = 0.0

    for (student in students) {
        val stats = viewModel.calculateStudentMetrics(student.registerNumber, student.totalSemesterHours, attendanceLogs)
        totalPctSum += stats.attendancePercentage
        when (stats.eligibilityStatus) {
            "ELIGIBLE" -> eligibleCount++
            "MEDICAL_REQUIRED" -> medicalCount++
            "CONTINUE_STUDY" -> continueStudyCount++
            else -> shortageCount++
        }
    }

    val overallAttendancePct = if (totalStudentsCount > 0) totalPctSum / totalStudentsCount else 80.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Admin Hub Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Quick Action Notifications Toggle
                    IconButton(onClick = { viewModel.navigateTo(AttendanceViewModel.Screen.NOTIFICATIONS_PANEL) }) {
                        BadgedBox(
                            badge = {
                                val notifications = viewModel.notificationList.collectAsState().value
                                if (notifications.isNotEmpty()) {
                                    Badge { Text(notifications.size.toString()) }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alarms")
                        }
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ExitToApp, contentDescription = "Exit Session", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AdminBottomNavigation(viewModel = viewModel, currentActive = "DASHBOARD")
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
            // Profile Welcome Board card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Welcome Back, Chief registrar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Smart Attendance Pro • Offline Cache Realtime DB Active",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // QUICK ACTION DIRECTORIES PANEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AttendanceViewModel.Screen.FACULTY_MANAGEMENT) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Manage Faculty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Add/edit teachers",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AttendanceViewModel.Screen.SUBJECT_MANAGEMENT) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Manage Subjects",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Track subjects & kids",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // OVERVIEW ANALYTICAL METRIC GAPS
            Text(
                text = "Academic Metrics Overview",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid of KPI cards with Material 3 standard elevations
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KPIMetricCard(
                    title = "Total Enrollment",
                    value = totalStudentsCount.toString(),
                    label = "Registered students",
                    icon = Icons.Default.Group,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KPIMetricCard(
                    title = "Overall Attendance",
                    value = String.format(Locale.getDefault(), "%.1f%%", overallAttendancePct),
                    label = "Monthly baseline",
                    icon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KPIMetricCard(
                    title = "Eligible Category",
                    value = eligibleCount.toString(),
                    label = "Above 75% goal",
                    icon = Icons.Default.CheckCircle,
                    accentColor = Color(0xFF10B981), // Green
                    modifier = Modifier.weight(1f)
                )
                KPIMetricCard(
                    title = "Medical Cases",
                    value = medicalCount.toString(),
                    label = "65% to 75% bounds",
                    icon = Icons.Default.MedicalServices,
                    accentColor = Color(0xFFF59E0B), // Orange
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KPIMetricCard(
                    title = "Continue study",
                    value = continueStudyCount.toString(),
                    label = "50% to 65% bounds",
                    icon = Icons.Default.HistoryEdu,
                    accentColor = Color(0xFFEF4444), // Crimson
                    modifier = Modifier.weight(1f)
                )
                KPIMetricCard(
                    title = "Shortage alert",
                    value = shortageCount.toString(),
                    label = "Under 50% critical",
                    icon = Icons.Default.Dangerous,
                    accentColor = Color(0xFFB91C1C), // Deep Red
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PIPES & CHARTS SECTION
            Text(
                text = "Eligibility Analytics Indicators",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hall-Ticket Eligibility Split",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw native Canvas Pie Chart
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val total = (eligibleCount + medicalCount + continueStudyCount + shortageCount).toFloat()
                            if (total == 0f) {
                                // Default fully green pie if empty
                                drawArc(
                                    color = Color(0xFF10B981),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = true
                                )
                            } else {
                                val s1 = (eligibleCount / total) * 360f
                                val s2 = (medicalCount / total) * 360f
                                val s3 = (continueStudyCount / total) * 360f
                                val s4 = (shortageCount / total) * 360f

                                drawArc(color = Color(0xFF10B981), startAngle = 0f, sweepAngle = s1, useCenter = true)
                                drawArc(color = Color(0xFFF59E0B), startAngle = s1, sweepAngle = s2, useCenter = true)
                                drawArc(color = Color(0xFFEF4444), startAngle = s1 + s2, sweepAngle = s3, useCenter = true)
                                drawArc(color = Color(0xFFB91C1C), startAngle = s1 + s2 + s3, sweepAngle = s4, useCenter = true)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Legend description labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LegendBadgeItem("Eligible", Color(0xFF10B981))
                        LegendBadgeItem("Medical", Color(0xFFF59E0B))
                        LegendBadgeItem("Cont. Study", Color(0xFFEF4444))
                        LegendBadgeItem("Shortage", Color(0xFFB91C1C))
                    }
                }
            }

            // WEEKLY CLASS ATTENDANCE LOG TREND CHART
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Weekly Cumulative Attendance Trend",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time class conduction averages",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Draw native Canvas Bar Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barWidth = 40.dp.toPx()
                        val spacing = 32.dp.toPx()
                        val barHeights = listOf(0.85f, 0.76f, 0.88f, 0.92f, 0.80f) // raw rates
                        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

                        barHeights.forEachIndexed { index, pctValue ->
                            val left = index * (barWidth + spacing) + spacing
                            val top = size.height - (size.height * pctValue * 0.8f) // scale to bounds
                            
                            // Draw dynamic column
                            drawRoundRect(
                                color = if (pctValue >= 0.85f) Color(0xFF0F4C81) else Color(0xFF1E88E5),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, size.height - top),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Mon (85%)", "Tue (76%)", "Wed (88%)", "Thu (92%)", "Fri (80%)").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MOCK CONTROL ACTIONS LOG
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AttendanceViewModel.Screen.QR_SCANNER) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Access Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Launch QR Badge Scanning Gate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "One-tap scan dynamic student cards for automated periods registration.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.61f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KPIMetricCard(
    title: String,
    value: String,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun LegendBadgeItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// Global Bottom Navigation component for administrative roles (Admin, Faculty)
@Composable
fun AdminBottomNavigation(
    viewModel: AttendanceViewModel,
    currentActive: String
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentActive == "DASHBOARD",
            onClick = { viewModel.navigateTo(AttendanceViewModel.Screen.ADMIN_DASHBOARD) },
            icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentActive == "STUDENTS",
            onClick = { viewModel.navigateTo(AttendanceViewModel.Screen.STUDENT_MANAGEMENT) },
            icon = { Icon(imageVector = Icons.Default.Groups, contentDescription = "Students") },
            label = { Text("Students", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentActive == "ATTENDANCE",
            onClick = { 
                viewModel.selectedDate.value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
                viewModel.loadAttendanceGrid()
                viewModel.navigateTo(AttendanceViewModel.Screen.ATTENDANCE_ENTRY) 
            },
            icon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Attendance") },
            label = { Text("Register", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentActive == "REPORTS",
            onClick = { viewModel.navigateTo(AttendanceViewModel.Screen.REPORTS_MODULE) },
            icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "Reports") },
            label = { Text("Reports", fontSize = 11.sp) }
        )
    }
}
