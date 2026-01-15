package com.app.wayra.ui.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.wayra.R
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.ui.plans.PlansViewModel
import com.app.wayra.ui.students.StudentsViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignPlanScreen(
    onNavigateBack: () -> Unit,
    onPlanAssigned: () -> Unit,
    studentsViewModel: StudentsViewModel = viewModel(),
    plansViewModel: PlansViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val students by studentsViewModel.students.observeAsState(emptyList())
    val plans by plansViewModel.plans.observeAsState(emptyList())

    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedPlan by remember { mutableStateOf<Plan?>(null) }
    var showStudentPicker by remember { mutableStateOf(false) }
    var showPlanPicker by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subscription_assign_plan)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Student Selector
            OutlinedCard(
                onClick = { showStudentPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.student_name),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedStudent?.getFullName() ?: stringResource(R.string.payment_select_student),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            // Plan Selector
            OutlinedCard(
                onClick = { showPlanPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.subscription_select_plan),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedPlan?.activityName ?: stringResource(R.string.subscription_no_plan),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        selectedPlan?.let { plan ->
                            Text(
                                text = formatCurrency(plan.price),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Assign Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (subscriptionViewModel.assignPlan(selectedStudent, selectedPlan)) {
                            onPlanAssigned()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedStudent != null && selectedPlan != null
            ) {
                Text(stringResource(R.string.subscription_assign_plan))
            }
        }
    }

    // Student Picker Dialog
    if (showStudentPicker) {
        AlertDialog(
            onDismissRequest = { showStudentPicker = false },
            title = { Text(stringResource(R.string.payment_select_student)) },
            text = {
                Column {
                    students.filter { it.active }.forEach { student ->
                        TextButton(
                            onClick = {
                                selectedStudent = student
                                showStudentPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(student.getFullName(), modifier = Modifier.fillMaxWidth())
                        }
                    }
                    if (students.none { it.active }) {
                        Text(stringResource(R.string.students_empty))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudentPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Plan Picker Dialog
    if (showPlanPicker) {
        AlertDialog(
            onDismissRequest = { showPlanPicker = false },
            title = { Text(stringResource(R.string.subscription_select_plan)) },
            text = {
                Column {
                    plans.forEach { plan ->
                        TextButton(
                            onClick = {
                                selectedPlan = plan
                                showPlanPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(plan.activityName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    formatCurrency(plan.price),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    if (plans.isEmpty()) {
                        Text(stringResource(R.string.plans_empty))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlanPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
    return format.format(amount)
}
