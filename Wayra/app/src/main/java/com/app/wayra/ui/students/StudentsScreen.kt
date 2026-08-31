package com.app.wayra.ui.students

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.ui.components.WayraBackground
import com.app.wayra.ui.theme.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow

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
                                contentDescription = stringResource(R.string.students_add)
                            )
                        }
                    },
                    colors = wayraTopAppBarColors()
                )
            },
            containerColor = Paper,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            StudentsContent(
                filteredStudents = filteredStudents,
                searchQuery = searchQuery,
                viewModel = viewModel,
                onNavigateToStudentDetail = onNavigateToStudentDetail,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                                contentDescription = stringResource(R.string.students_add)
                            )
                        }
                    },
                    colors = wayraTopAppBarColors()
                )
            },
            containerColor = Paper,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            StudentsContent(
                filteredStudents = filteredStudents,
                searchQuery = searchQuery,
                viewModel = viewModel,
                onNavigateToStudentDetail = onNavigateToStudentDetail,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
    modifier: Modifier = Modifier
) {
    WayraBackground(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp)
        ) {

            // Search Bar
            if (filteredStudents.isNotEmpty()) {
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

                Spacer(modifier = Modifier.height(16.dp))

                SectionLabel(
                    text = if (filteredStudents.size == 1) "1 alumno"
                           else "${filteredStudents.size} alumnos"
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(filteredStudents) { index, studentWithPlan ->
                        StudentRow(
                            studentWithPlan = studentWithPlan,
                            isFirst = index == 0,
                            isLast = index == filteredStudents.lastIndex,
                            onClick = { onNavigateToStudentDetail(studentWithPlan.student.id) }
                        )
                    }
                }
            } else {
                // Students List
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_runner),
                            contentDescription = null,
                            tint = InkSubtle,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.students_empty),
                            color = Ink,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Presiona el botón + para agregar un alumno",
                            color = Ink,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentRow(
    studentWithPlan: StudentWithPlan,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val student = studentWithPlan.student
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 12.dp else 0.dp,
        topEnd = if (isFirst) 12.dp else 0.dp,
        bottomStart = if (isLast) 12.dp else 0.dp,
        bottomEnd = if (isLast) 12.dp else 0.dp
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceCard)
            .clickable(onClick = onClick)
    ) {
        if (!isFirst) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 62.dp),
                thickness = 1.dp,
                color = BorderSoft
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Inicial en cuadrado redondeado: el circulo perfecto es el
            // avatar por defecto de cualquier plantilla.
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (student.active) WayraOrangeSoft else SurfaceAlt,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1).uppercase(),
                    fontFamily = AgenorNeue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (student.active) WayraOrangeDark else InkSubtle
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = student.getFullName(),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 15.sp,
                        color = if (student.active) Ink else InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StatusDot(tone = if (student.active) Tone.Success else Tone.Neutral)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = student.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (studentWithPlan.planName != null) {
                StatusBadge(
                    text = studentWithPlan.planName,
                    tone = if (student.active) Tone.Brand else Tone.Neutral
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron),
                contentDescription = null,
                tint = InkSubtle,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}