package com.app.wayra.ui.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.wayra.R
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.data.model.Subscription
import com.app.wayra.data.repository.SubscriptionRepository
import com.app.wayra.ui.components.CustomSnackbarHost
import com.app.wayra.ui.components.showErrorSnackbar
import com.app.wayra.ui.components.showSuccessSnackbar
import com.app.wayra.ui.plans.PlansViewModel
import com.app.wayra.ui.subscriptions.SubscriptionViewModel
import com.app.wayra.ui.theme.Cream
import com.app.wayra.ui.theme.WayraOrange
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: String,
    onNavigateBack: () -> Unit,
    viewModel: StudentsViewModel = viewModel(),
    plansViewModel: PlansViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val students by viewModel.students.observeAsState(emptyList())
    val student = students.find { it.id == studentId }
    val plans by plansViewModel.plans.observeAsState(emptyList())

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para la suscripción activa y plan actual
    var activeSubscription by remember { mutableStateOf<Subscription?>(null) }
    var currentPlan by remember { mutableStateOf<Plan?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val subscriptionRepository = remember { SubscriptionRepository() }

    // Estado para historial de pagos
    var studentPayments by remember { mutableStateOf<List<com.app.wayra.data.model.Payment>>(emptyList()) }
    val paymentRepository = remember { com.app.wayra.data.repository.PaymentRepository() }

    // Cargar suscripción activa cuando cambia el studentId o refreshTrigger
    LaunchedEffect(studentId, refreshTrigger) {
        try {
            val subscription = subscriptionRepository.getActiveSubscription(studentId)
            activeSubscription = subscription
        } catch (_: Exception) {
            // Manejar error sin crashear
            activeSubscription = null
        }
    }

    // Buscar el plan correspondiente cuando cambia la suscripción o la lista de planes
    LaunchedEffect(activeSubscription, plans) {
        currentPlan = activeSubscription?.let { subscription ->
            plans.find { it.id == subscription.planId }
        }
    }

    // Cargar pagos del estudiante
    LaunchedEffect(studentId, refreshTrigger) {
        paymentRepository.getPaymentsByStudent(studentId)
            .catch { e ->
                // Manejar error sin crashear
                studentPayments = emptyList()
            }
            .collect { payments ->
                studentPayments = payments
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Alumno") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WayraOrange,
                    titleContentColor = Cream,
                    navigationIconContentColor = Cream,
                    actionIconContentColor = Cream
                )
            )
        },
        snackbarHost = { CustomSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Student Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow(label = "Nombre", value = student?.getFullName() ?: "Cargando...")
                    DetailRow(label = "Email", value = student?.email ?: "-")
                    DetailRow(label = "Teléfono", value = student?.phone ?: "-")

                    Surface(
                        color = if (student?.active == true) Color(0xFF4CAF50) else Color(0xFFFF5722),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (student?.active == true)
                                stringResource(R.string.students_active)
                            else
                                stringResource(R.string.students_inactive),
                            color = Cream,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Subscription Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Suscripción Actual",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentPlan != null && activeSubscription != null) {
                        DetailRow(label = "Plan", value = currentPlan!!.activityName)
                        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                        DetailRow(label = "Precio", value = formatter.format(currentPlan!!.price))
                        activeSubscription!!.startDate?.let { startDate ->
                            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-AR"))
                            DetailRow(label = "Fecha de inicio", value = dateFormat.format(Date(startDate)))
                        }
                    } else {
                        Text(
                            text = "Sin plan asignado",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Payment History
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Historial de Pagos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (studentPayments.isEmpty()) {
                        Text(
                            text = "No hay pagos registrados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            studentPayments.take(5).forEach { payment ->
                                PaymentHistoryItem(payment = payment)
                            }

                            if (studentPayments.size > 5) {
                                Text(
                                    text = "Mostrando últimos 5 de ${studentPayments.size} pagos",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de editar
        if (showEditDialog && student != null) {
            EditStudentDialog(
                student = student,
                plans = plans,
                currentPlan = currentPlan,
                onDismiss = { showEditDialog = false },
                onSave = { updatedStudent, selectedPlan ->
                    showEditDialog = false
                    coroutineScope.launch {
                        val result = viewModel.updateStudent(studentId, updatedStudent)
                        if (result.isSuccess) {
                            // Si se seleccionó un plan, actualizar suscripción
                            if (selectedPlan != null && selectedPlan.id != currentPlan?.id) {
                                // Si ya tiene un plan, usar changePlan(); si no, usar assignPlan()
                                val subscriptionResult = if (currentPlan != null) {
                                    subscriptionViewModel.changePlan(studentId, selectedPlan)
                                } else {
                                    subscriptionViewModel.assignPlan(updatedStudent, selectedPlan)
                                }

                                if (subscriptionResult) {
                                    // Refrescar la suscripción actual
                                    refreshTrigger++
                                }
                            }
                            viewModel.refreshData()
                            snackbarHostState.showSuccessSnackbar("Alumno actualizado")
                        } else {
                            snackbarHostState.showErrorSnackbar("Error al actualizar")
                        }
                    }
                }
            )
        }

        // Diálogo de eliminar
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isDeleting) {
                        showDeleteDialog = false
                    }
                },
                title = { Text("Eliminar Alumno") },
                text = { Text("¿Estás seguro de que deseas eliminar a ${student?.getFullName()}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                isDeleting = true
                                val result = viewModel.deleteStudent(studentId)
                                isDeleting = false
                                if (result.isSuccess) {
                                    showDeleteDialog = false
                                    snackbarHostState.showSuccessSnackbar("Alumno eliminado")
                                    onNavigateBack()
                                } else {
                                    showDeleteDialog = false
                                    snackbarHostState.showErrorSnackbar("Error al eliminar")
                                }
                            }
                        },
                        enabled = !isDeleting
                    ) {
                        if (isDeleting) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Red,
                                    strokeWidth = 2.dp
                                )
                                Text("Eliminando...", color = Color.Red)
                            }
                        } else {
                            Text("Eliminar", color = Color.Red)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false },
                        enabled = !isDeleting
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun EditStudentDialog(
    student: Student,
    plans: List<Plan>,
    currentPlan: Plan?,
    onDismiss: () -> Unit,
    onSave: (Student, Plan?) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }
    var surname by remember { mutableStateOf(student.surname) }
    var email by remember { mutableStateOf(student.email) }
    var phone by remember { mutableStateOf(student.phone) }
    var active by remember { mutableStateOf(student.active) }
    var selectedPlan by remember { mutableStateOf(currentPlan) }
    var showPlanPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Alumno") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Apellido") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    singleLine = true
                )

                // Selector de Plan
                OutlinedCard(
                    onClick = { showPlanPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Plan",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = selectedPlan?.activityName ?: "Sin plan asignado",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Activo", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        student.copy(
                            name = name,
                            surname = surname,
                            email = email,
                            phone = phone,
                            active = active
                        ),
                        selectedPlan
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Diálogo de selección de plan
    if (showPlanPicker) {
        AlertDialog(
            onDismissRequest = { showPlanPicker = false },
            title = { Text("Cambiar Plan") },
            text = {
                Column {
                    if (plans.isEmpty()) {
                        Text("No hay planes disponibles")
                    } else {
                        // Lista de planes
                        plans.forEach { plan ->
                            val isCurrentPlan = plan.id == currentPlan?.id
                            OutlinedCard(
                                onClick = {
                                    selectedPlan = plan
                                    showPlanPicker = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = plan.activityName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isCurrentPlan) {
                                            Text(
                                                text = "(Actual)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                    Text(
                                        text = formatter.format(plan.price),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
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

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun PaymentHistoryItem(payment: com.app.wayra.data.model.Payment) {
    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = when (payment.status) {
                com.app.wayra.data.model.PaymentStatus.PAGADO -> Color(0xFFE8F5E9)
                com.app.wayra.data.model.PaymentStatus.PENDIENTE -> Color(0xFFFFF3E0)
                com.app.wayra.data.model.PaymentStatus.VENCIDO -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Monto
                Text(
                    text = currencyFormat.format(payment.amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Fecha
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = when (payment.status) {
                            com.app.wayra.data.model.PaymentStatus.PAGADO -> "Pagado:"
                            else -> "Vence:"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (payment.status) {
                            com.app.wayra.data.model.PaymentStatus.PAGADO ->
                                payment.paymentDate?.let { dateFormat.format(Date(it)) } ?: "-"
                            else ->
                                payment.dueDate?.let { dateFormat.format(Date(it)) } ?: "-"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Método de pago (solo si está pagado)
                if (payment.status == com.app.wayra.data.model.PaymentStatus.PAGADO && payment.paymentMethod != null) {
                    Text(
                        text = when (payment.paymentMethod) {
                            com.app.wayra.data.model.PaymentMethod.EFECTIVO -> "Efectivo"
                            com.app.wayra.data.model.PaymentMethod.TRANSFERENCIA -> "Transferencia"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Badge de estado
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (payment.status) {
                    com.app.wayra.data.model.PaymentStatus.PAGADO -> Color(0xFF4CAF50)
                    com.app.wayra.data.model.PaymentStatus.PENDIENTE -> Color(0xFFFF9800)
                    com.app.wayra.data.model.PaymentStatus.VENCIDO -> Color(0xFFE53935)
                }
            ) {
                Text(
                    text = when (payment.status) {
                        com.app.wayra.data.model.PaymentStatus.PAGADO -> "Pagado"
                        com.app.wayra.data.model.PaymentStatus.PENDIENTE -> "Pendiente"
                        com.app.wayra.data.model.PaymentStatus.VENCIDO -> "Vencido"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Cream
                )
            }
        }
    }
}
