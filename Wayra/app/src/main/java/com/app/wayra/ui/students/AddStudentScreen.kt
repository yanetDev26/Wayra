package com.app.wayra.ui.students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.wayra.R
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.data.model.Subscription
import com.app.wayra.ui.plans.PlansViewModel
import com.app.wayra.ui.subscriptions.SubscriptionViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(
    onNavigateBack: () -> Unit,
    onStudentAdded: () -> Unit,
    viewModel: StudentsViewModel = viewModel(),
    plansViewModel: PlansViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var selectedPlan by remember { mutableStateOf<Plan?>(null) }
    var showPlanPicker by remember { mutableStateOf(false) }

    val plans by plansViewModel.plans.observeAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_student_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.student_email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.student_phone)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de Plan
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
                            text = "Plan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedPlan?.activityName ?: "Sin plan asignado",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        selectedPlan?.let { plan ->
                            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                            Text(
                                text = formatter.format(plan.price),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.student_active),
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        if (name.isNotBlank() && surname.isNotBlank() && email.isNotBlank()) {
                            coroutineScope.launch {
                                val student = Student(
                                    name = name,
                                    surname = surname,
                                    email = email,
                                    phone = phone,
                                    registrationDate = Timestamp.now(),
                                    active = isActive
                                )
                                val result = viewModel.addStudent(student)
                                if (result.isSuccess) {
                                    // Si se seleccionó un plan, crear la suscripción
                                    val studentId = result.getOrNull()
                                    if (studentId != null && selectedPlan != null) {
                                        Subscription(
                                            studentId = studentId,
                                            planId = selectedPlan!!.id,
                                            startDate = System.currentTimeMillis(),
                                            active = true
                                        )
                                        subscriptionViewModel.assignPlan(
                                            student.copy(id = studentId),
                                            selectedPlan
                                        )
                                    }
                                    onStudentAdded()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank() && surname.isNotBlank() && email.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Diálogo de selección de plan
        if (showPlanPicker) {
            AlertDialog(
                onDismissRequest = { showPlanPicker = false },
                title = { Text("Seleccionar Plan") },
                text = {
                    Column {
                        if (plans.isEmpty()) {
                            Text("No hay planes disponibles")
                        } else {
                            // Opción para no asignar plan
                            OutlinedCard(
                                onClick = {
                                    selectedPlan = null
                                    showPlanPicker = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Sin plan",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "No asignar plan al estudiante",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Lista de planes
                            plans.forEach { plan ->
                                OutlinedCard(
                                    onClick = {
                                        selectedPlan = plan
                                        showPlanPicker = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = plan.activityName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                        Text(
                                            text = formatter.format(plan.price),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (plan.description.isNotBlank()) {
                                            Text(
                                                text = plan.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPlanPicker = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}
