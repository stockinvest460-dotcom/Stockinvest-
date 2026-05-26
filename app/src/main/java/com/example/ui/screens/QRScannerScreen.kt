package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(viewModel: AttendanceViewModel) {
    val students by viewModel.students.collectAsState()
    val qrScanningState by viewModel.qrScanningState.collectAsState()

    var isSimulateExpanded by remember { mutableStateOf(false) }
    var selectedStudentReg by remember { mutableStateOf("") }

    // Setup scanning laser infinite vertical animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College ID QR Gate", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AttendanceViewModel.Screen.ADMIN_DASHBOARD) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                .background(Color(0xFF0B0F19)) // Solid immersive dark background
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Explanatory guidance
            Text(
                text = "Align Student Card QR Badge",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "Hold card steady. Scanner registers periods 1-8 automatically upon validation match.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // VIEW FINDER CAMERA BOX FRAME
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(width = 3.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Mock Camera scanning noise / visuals
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.size(140.dp)
                )

                // Laser Scan Animation overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val laserY = size.height * laserYOffset
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(20f, laserY),
                        end = Offset(size.width - 20f, laserY),
                        strokeWidth = 6f
                    )
                }

                // Four corner bracket details
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Custom corners
                }
            }

            // SIMULATOR TRIGGER SWITCH BUTTON
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Button(
                    onClick = { isSimulateExpanded = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.FlipCameraAndroid, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Card to Simulate Scan", fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = isSimulateExpanded,
                    onDismissRequest = { isSimulateExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    students.take(10).forEach { StudentItem ->
                        DropdownMenuItem(
                            text = { Text("${StudentItem.name} (${StudentItem.registerNumber})", fontSize = 13.sp) },
                            onClick = {
                                selectedStudentReg = StudentItem.registerNumber
                                viewModel.simulateQRScan(StudentItem.registerNumber)
                                isSimulateExpanded = false
                            }
                        )
                    }
                }
            }

            // RESULTS LOG CARD
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (qrScanningState?.startsWith("Success") == true) Color(0xFF14532D) else Color(0xFF1E293B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Decoder Feedback Log",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = qrScanningState ?: "Awaiting college badge scan...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
