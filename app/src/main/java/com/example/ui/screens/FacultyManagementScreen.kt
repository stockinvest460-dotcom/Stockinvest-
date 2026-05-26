package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.Faculty
import com.example.ui.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyManagementScreen(viewModel: AttendanceViewModel) {
    val faculties by viewModel.faculties.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var searchVal by remember { mutableStateOf("") }

    val filteredFaculties = faculties.filter {
        it.name.contains(searchVal, ignoreCase = true) ||
                it.facultyId.contains(searchVal, ignoreCase = true) ||
                it.department.contains(searchVal, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Faculty Directory Hub", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                modifier = Modifier.testTag("add_faculty_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Faculty")
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
            // Search field
            OutlinedTextField(
                value = searchVal,
                onValueChange = { searchVal = it },
                placeholder = { Text("Search by name or department...") },
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

            if (filteredFaculties.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No faculty profiles found.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredFaculties, key = { it.facultyId }) { faculty ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = faculty.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${faculty.designation} • ${faculty.facultyId}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = faculty.email,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Department badge
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(faculty.department, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteFaculty(faculty) },
                                    modifier = Modifier.testTag("delete_${faculty.facultyId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var id by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var selectedDept by remember { mutableStateOf("COMPUTER") }
        var email by remember { mutableStateOf("") }
        var designation by remember { mutableStateOf("Professor") }

        var isDeptDropdown by remember { mutableStateOf(false) }
        var isDesigDropdown by remember { mutableStateOf(false) }

        val designationalList = listOf("Professor", "Associate Professor", "Assistant Professor", "Lecturer", "Senior Lecturer")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Register New Faculty", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Faculty Code ID") },
                        placeholder = { Text("e.g. FAC106") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("faculty_id_input")
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name Name") },
                        placeholder = { Text("e.g. Dr. Ada Lovelace") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("faculty_name_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("academic Email") },
                        placeholder = { Text("e.g. ada.l@college.edu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("faculty_email_input")
                    )

                    // Department dropdown select
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

                    // Designation dropdown select
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = designation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Designation Post") },
                            trailingIcon = {
                                IconButton(onClick = { isDesigDropdown = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = isDesigDropdown,
                            onDismissRequest = { isDesigDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            designationalList.forEach { post ->
                                DropdownMenuItem(
                                    text = { Text(post) },
                                    onClick = {
                                        designation = post
                                        isDesigDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (id.isNotBlank() && name.isNotBlank() && email.isNotBlank()) {
                            viewModel.addFaculty(
                                Faculty(
                                    facultyId = id,
                                    name = name,
                                    department = selectedDept,
                                    email = email,
                                    designation = designation
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_faculty_btn")
                ) {
                    Text("Register Profile")
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
