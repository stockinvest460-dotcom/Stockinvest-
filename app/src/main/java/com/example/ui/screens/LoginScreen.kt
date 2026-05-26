package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AttendanceViewModel

@Composable
fun LoginScreen(viewModel: AttendanceViewModel) {
    var username by remember { mutableStateOf(viewModel.savedUsername.value.ifEmpty { "admin" }) }
    var password by remember { mutableStateOf("password123") }
    var selectedRoleIndex by remember { mutableStateOf(0) } // 0: Admin, 1: Staff, 2: Student
    var showPassword by remember { mutableStateOf(false) }
    var isRememberMe by remember { mutableStateOf(viewModel.rememberMe.value) }

    var forgotPasswordAlert by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    val roles = listOf("ADMIN", "STAFF", "STUDENT")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (MaterialTheme.colorScheme.primary == Color(0xFF0F4C81)) {
                        listOf(Color(0xFFEBF4FA), Color(0xFFFFFFFF))
                    } else {
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 420.dp)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding Brand Title
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Smart Attendance Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Smart Attendance Pro",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Integrated Academic ERP Panel",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Form container
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign In Security",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Role selector tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        roles.forEachIndexed { index, role ->
                            val isSelected = selectedRoleIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .clickable {
                                        selectedRoleIndex = index
                                        // Auto adjust initial mock usernames depending on selected role
                                        username = when (index) {
                                            0 -> "admin"
                                            1 -> "professor_smith"
                                            else -> "CS202601"
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            loginError = false
                        },
                        label = { Text(if (selectedRoleIndex == 2) "Register Number" else "Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selectedRoleIndex == 2) Icons.Default.Badge else Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            loginError = false
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remember me and Forgot password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                isRememberMe = !isRememberMe
                                viewModel.rememberMe.value = isRememberMe
                            }
                        ) {
                            Checkbox(
                                checked = isRememberMe,
                                onCheckedChange = {
                                    isRememberMe = it
                                    viewModel.rememberMe.value = it
                                }
                            )
                            Text(
                                text = "Remember",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = "Forgot Action?",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { forgotPasswordAlert = true }
                                .padding(4.dp)
                        )
                    }

                    if (loginError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Invalid register number or username! Try 'admin', 'professor_smith', or 'CS202601'.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit login button
                    Button(
                        onClick = {
                            val success = viewModel.login(username, roles[selectedRoleIndex])
                            if (!success) {
                                loginError = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button")
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Access Console",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Forgot Password simulation Dialog
    if (forgotPasswordAlert) {
        AlertDialog(
            onDismissRequest = { forgotPasswordAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Session Security Hack")
                }
            },
            text = {
                Text(
                    text = "For demonstration in college ERP sandboxes, please log in using the built-in credentials:\n\n" +
                            "• Admin Role: username 'admin'\n" +
                            "• Faculty Role: username 'professor_smith'\n" +
                            "• Student Role: register number 'CS202601' or 'CS202605'.\n\n" +
                            "No external connection needed. Password is default 'password123' for all characters."
                )
            },
            confirmButton = {
                TextButton(onClick = { forgotPasswordAlert = false }) {
                    Text("Understood")
                }
            }
        )
    }
}
