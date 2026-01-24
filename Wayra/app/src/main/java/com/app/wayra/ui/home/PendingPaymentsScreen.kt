package com.app.wayra.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.data.model.Student
import com.app.wayra.data.repository.PaymentRepository
import com.app.wayra.data.repository.StudentRepository
import com.app.wayra.ui.components.CustomSnackbarHost
import com.app.wayra.ui.components.WayraBackground
import com.app.wayra.ui.components.showErrorSnackbar
import com.app.wayra.ui.components.showSuccessSnackbar
import com.app.wayra.ui.theme.Cream
import com.app.wayra.ui.theme.WayraOrange
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StudentWithPayment(
    val student: Student,
    val payment: Payment
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingPaymentsScreen(
    periodType: String, // "this_month" o "last_month"
    onNavigateBack: () -> Unit
) {
    var studentsWithPayments by remember { mutableStateOf<List<StudentWithPayment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPayment by remember { mutableStateOf<StudentWithPayment?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val paymentRepository = remember { PaymentRepository() }
    val studentRepository = remember { StudentRepository() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(periodType) {
        isLoading = true

        // Obtener rango de fechas según el período
        val calendar = Calendar.getInstance()

        if (periodType == "last_month") {
            calendar.add(Calendar.MONTH, -1)
        }

        // Inicio del mes
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfMonth = calendar.timeInMillis

        // Fin del mes
        val endCalendar = Calendar.getInstance()
        if (periodType == "last_month") {
            endCalendar.add(Calendar.MONTH, -1)
        }
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        val endOfMonth = endCalendar.timeInMillis

        // Obtener todos los pagos pendientes y filtrar por fecha
        val allPayments = paymentRepository.getPendingPayments()
        val filteredPayments = allPayments.filter { payment ->
            val dueDate = payment.dueDate ?: 0L
            dueDate in startOfMonth..endOfMonth
        }

        // Obtener todos los estudiantes
        val allStudents = studentRepository.getAllStudents()

        // Combinar estudiantes con sus pagos pendientes
        val studentsWithPaymentsList = filteredPayments.mapNotNull { payment ->
            val student = allStudents.find { it.id == payment.studentId }
            if (student != null) {
                StudentWithPayment(student, payment)
            } else {
                null
            }
        }.sortedBy { it.student.name }

        studentsWithPayments = studentsWithPaymentsList
        isLoading = false
    }

    val title = if (periodType == "this_month") {
        "Pendientes Este Mes"
    } else {
        "Pendientes Mes Pasado"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WayraOrange,
                    titleContentColor = Cream,
                    navigationIconContentColor = Cream
                )
            )
        },
        snackbarHost = { CustomSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        WayraBackground(
            modifier = Modifier.padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (studentsWithPayments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay pagos pendientes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(studentsWithPayments) { item ->
                            PendingPaymentCard(
                                studentWithPayment = item,
                                onClick = {
                                    selectedPayment = item
                                    showPaymentDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación de pago
        if (showPaymentDialog && selectedPayment != null) {
            PaymentConfirmationDialog(
                studentWithPayment = selectedPayment!!,
                onDismiss = { showPaymentDialog = false },
                onConfirm = { paymentMethod, notes, paymentPeriod ->
                    coroutineScope.launch {
                        // Calcular la nueva fecha de vencimiento según el período seleccionado
                        val calendar = Calendar.getInstance()
                        when (paymentPeriod) {
                            PaymentPeriod.LAST_MONTH -> calendar.add(Calendar.MONTH, -1)
                            PaymentPeriod.CURRENT_MONTH -> { /* No hacer nada, usar mes actual */ }
                            PaymentPeriod.NEXT_MONTH -> calendar.add(Calendar.MONTH, 1)
                        }
                        // Establecer al último día del mes seleccionado
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                        calendar.set(Calendar.SECOND, 59)
                        val newDueDate = calendar.timeInMillis

                        // Actualizar el pago a PAGADO
                        val updatedPayment = selectedPayment!!.payment.copy(
                            status = PaymentStatus.PAGADO,
                            paymentDate = System.currentTimeMillis(),
                            dueDate = newDueDate, // Actualizar la fecha de vencimiento según el período
                            paymentMethod = paymentMethod,
                            notes = notes.ifBlank { selectedPayment!!.payment.notes }
                        )

                        val result = paymentRepository.updatePayment(
                            selectedPayment!!.payment.id,
                            updatedPayment
                        )

                        if (result.isSuccess) {
                            snackbarHostState.showSuccessSnackbar("Pago registrado exitosamente")
                            showPaymentDialog = false
                            // Recargar la lista
                            isLoading = true
                            // Repetir la lógica de carga
                            val calendar = Calendar.getInstance()
                            if (periodType == "last_month") {
                                calendar.add(Calendar.MONTH, -1)
                            }
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            val startOfMonth = calendar.timeInMillis

                            val endCalendar = Calendar.getInstance()
                            if (periodType == "last_month") {
                                endCalendar.add(Calendar.MONTH, -1)
                            }
                            endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                            endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                            endCalendar.set(Calendar.MINUTE, 59)
                            endCalendar.set(Calendar.SECOND, 59)
                            val endOfMonth = endCalendar.timeInMillis

                            val allPayments = paymentRepository.getPendingPayments()
                            val filteredPayments = allPayments.filter { payment ->
                                val dueDate = payment.dueDate ?: 0L
                                dueDate in startOfMonth..endOfMonth
                            }

                            val allStudents = studentRepository.getAllStudents()
                            val studentsWithPaymentsList = filteredPayments.mapNotNull { payment ->
                                val student = allStudents.find { it.id == payment.studentId }
                                if (student != null) {
                                    StudentWithPayment(student, payment)
                                } else {
                                    null
                                }
                            }.sortedBy { it.student.name }

                            studentsWithPayments = studentsWithPaymentsList
                            isLoading = false
                        } else {
                            snackbarHostState.showErrorSnackbar("Error al registrar el pago")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PendingPaymentCard(
    studentWithPayment: StudentWithPayment,
    onClick: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-AR"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Cream
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = studentWithPayment.student.getFullName(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (studentWithPayment.payment.notes.isNotBlank()) {
                        Text(
                            text = studentWithPayment.payment.notes,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = formatter.format(studentWithPayment.payment.amount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WayraOrange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vencimiento:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                studentWithPayment.payment.dueDate?.let { dueDate ->
                    val isOverdue = dueDate < System.currentTimeMillis()
                    Text(
                        text = dateFormat.format(Date(dueDate)),
                        fontSize = 12.sp,
                        color = if (isOverdue) Color.Red else Color.Gray,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

enum class PaymentPeriod {
    LAST_MONTH,
    CURRENT_MONTH,
    NEXT_MONTH
}

@Composable
fun PaymentConfirmationDialog(
    studentWithPayment: StudentWithPayment,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod, String, PaymentPeriod) -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.EFECTIVO) }
    var selectedPeriod by remember { mutableStateOf(PaymentPeriod.CURRENT_MONTH) }
    var notes by remember { mutableStateOf("") }
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Pago") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información del alumno y monto
                Column {
                    Text(
                        text = "Alumno:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = studentWithPayment.student.getFullName(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Monto:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatter.format(studentWithPayment.payment.amount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = WayraOrange
                    )
                }

                // Selector de período
                Column {
                    Text(
                        text = "Corresponde al mes:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentPeriod.entries.forEach { period ->
                            val isSelected = selectedPeriod == period
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPeriod = period },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) WayraOrange else Color.LightGray
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Text(
                                    text = when (period) {
                                        PaymentPeriod.LAST_MONTH -> "Anterior"
                                        PaymentPeriod.CURRENT_MONTH -> "Actual"
                                        PaymentPeriod.NEXT_MONTH -> "Próximo"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Cream else Color.Black
                                )
                            }
                        }
                    }
                }

                // Selector de método de pago
                Column {
                    Text(
                        text = "Método de pago:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethod.entries.forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPaymentMethod = method },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) WayraOrange else Color.LightGray
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Text(
                                    text = when (method) {
                                        PaymentMethod.EFECTIVO -> "Efectivo"
                                        PaymentMethod.TRANSFERENCIA -> "Transferencia"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Cream else Color.Black
                                )
                            }
                        }
                    }
                }

                // Campo de notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedPaymentMethod, notes, selectedPeriod)
                }
            ) {
                Text("Confirmar Pago")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
