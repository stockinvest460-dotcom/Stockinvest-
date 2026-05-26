package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AttendanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: AttendanceViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)).togetherWith(
                                    fadeOut(animationSpec = tween(220))
                                )
                            },
                            label = "screen_transitions"
                        ) { screen ->
                            when (screen) {
                                AttendanceViewModel.Screen.LOGIN -> LoginScreen(viewModel)
                                AttendanceViewModel.Screen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel)
                                AttendanceViewModel.Screen.STUDENT_MANAGEMENT -> StudentManagementScreen(viewModel)
                                AttendanceViewModel.Screen.ATTENDANCE_ENTRY -> AttendanceEntryScreen(viewModel)
                                AttendanceViewModel.Screen.REPORTS_MODULE -> ReportsScreen(viewModel)
                                AttendanceViewModel.Screen.STUDENT_PORTAL -> StudentPortalScreen(viewModel)
                                AttendanceViewModel.Screen.QR_SCANNER -> QRScannerScreen(viewModel)
                                AttendanceViewModel.Screen.NOTIFICATIONS_PANEL -> NotificationsScreen(viewModel)
                                AttendanceViewModel.Screen.FACULTY_MANAGEMENT -> FacultyManagementScreen(viewModel)
                                AttendanceViewModel.Screen.SUBJECT_MANAGEMENT -> SubjectManagementScreen(viewModel)
                                else -> LoginScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
