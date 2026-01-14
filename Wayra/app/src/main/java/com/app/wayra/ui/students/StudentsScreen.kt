package com.app.wayra.ui.students

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.ui.theme.WayraOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: StudentsViewModel,
    onNavigateToAddStudent: () -> Unit,
    onNavigateToStudentDetail: (String) -> Unit,
    showTopBar: Boolean = true
) {
    val filteredStudents by viewModel.filteredStudents.observeAsState(emptyList())
    val searchQuery by viewModel.searchQuery.observeAsState("")

    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.students_title)) },
                    actions = {
                        IconButton(onClick = onNavigateToAddStudent) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.students_add),
                                tint = WayraOrange
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            StudentsContent(
                filteredStudents = filteredStudents,
                searchQuery = searchQuery,
                viewModel = viewModel,
                onNavigateToStudentDetail = onNavigateToStudentDetail,
                onNavigateToAddStudent = onNavigateToAddStudent,
                showTitle = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.students_title)) },
                    actions = {
                        IconButton(onClick = onNavigateToAddStudent) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.students_add),
                                tint = WayraOrange
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            StudentsContent(
                filteredStudents = filteredStudents,
                searchQuery = searchQuery,
                viewModel = viewModel,
                onNavigateToStudentDetail = onNavigateToStudentDetail,
                onNavigateToAddStudent = onNavigateToAddStudent,
                showTitle = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun StudentsContent(
    filteredStudents: List<StudentWithPlan>,
    searchQuery: String,
    viewModel: StudentsViewModel,
    onNavigateToStudentDetail: (String) -> Unit,
    onNavigateToAddStudent: () -> Unit = {},
    showTitle: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Título solo si no hay topBar
        if (showTitle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.students_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onNavigateToAddStudent,
                    modifier = Modifier
                        .background(
                            color = WayraOrange,
                            shape = CircleShape
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.students_add),
                        tint = Color.White
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchStudents(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    stringResource(R.string.students_search),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedIndicatorColor = WayraOrange,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                focusedLeadingIconColor = WayraOrange,
                cursorColor = WayraOrange
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Students List
        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏃",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.students_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredStudents) { studentWithPlan ->
                    StudentItemModern(
                        studentWithPlan = studentWithPlan,
                        onClick = { onNavigateToStudentDetail(studentWithPlan.student.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentItemModern(
    studentWithPlan: StudentWithPlan,
    onClick: () -> Unit
) {
    val student = studentWithPlan.student
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = if (student.active)
                            WayraOrange.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1).uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (student.active) WayraOrange else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Student info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = student.getFullName(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (student.active) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                shape = CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "📧 ${student.email}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "📱 ${student.phone}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Plan Badge
                if (studentWithPlan.planName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = WayraOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏋️ ",
                                fontSize = 12.sp
                            )
                            Text(
                                text = studentWithPlan.planName,
                                fontSize = 12.sp,
                                color = WayraOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}