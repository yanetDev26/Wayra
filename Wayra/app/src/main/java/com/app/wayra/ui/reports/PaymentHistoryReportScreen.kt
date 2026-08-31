package com.app.wayra.ui.reports

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.data.model.Student
import com.app.wayra.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryReportScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val allPayments by viewModel.allPayments.observeAsState(emptyList())
    val allStudents by viewModel.allStudents.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    // Estados para filtros
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var showFilters by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadPaymentHistory()
    }

    // Aplicar filtros
    val filteredPayments = remember(allPayments, selectedStudent, selectedStatus, selectedMethod) {
        allPayments.filter { payment ->
            val matchesStudent = selectedStudent == null || payment.studentId == selectedStudent?.id
            val matchesStatus = selectedStatus == null || payment.status == selectedStatus
            val matchesMethod = selectedMethod == null || payment.paymentMethod == selectedMethod
            matchesStudent && matchesStatus && matchesMethod
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Pagos") },
                colors = wayraTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Filtros",
                            tint = OnDark
                        )
                    }
                }
            )
        },
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WayraOrange)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                ) {
                // Resumen
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = WayraOrange
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${filteredPayments.size}",
                                fontFamily = AgenorNeue,
                                fontSize = 32.sp,
                                color = OnDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (filteredPayments.size == 1) "Pago encontrado" else "Pagos encontrados",
                                fontSize = 14.sp,
                                color = OnDark.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Total
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSoft),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceCard
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                        .format(filteredPayments.sumOf { it.amount }),
                                    fontFamily = AgenorNeue,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WayraOrange
                                )
                                Text(
                                    text = "Total",
                                    fontSize = 12.sp,
                                    color = Ink
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSoft),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceCard
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${filteredPayments.count { it.status == PaymentStatus.PAGADO }}",
                                    fontFamily = AgenorNeue,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WayraOrange
                                )
                                Text(
                                    text = "Pagados",
                                    fontSize = 12.sp,
                                    color = Ink
                                )
                            }
                        }
                    }
                }

                // Filtros
                if (showFilters) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSoft),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceCard
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Filtros",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Filtro por estudiante
                                StudentDropdown(
                                    students = allStudents,
                                    selectedStudent = selectedStudent,
                                    onStudentSelected = { selectedStudent = it }
                                )

                                // Filtro por estado
                                StatusDropdown(
                                    selectedStatus = selectedStatus,
                                    onStatusSelected = { selectedStatus = it }
                                )

                                // Filtro por método de pago
                                PaymentMethodDropdown(
                                    selectedMethod = selectedMethod,
                                    onMethodSelected = { selectedMethod = it }
                                )

                                // Botón para limpiar filtros
                                if (selectedStudent != null || selectedStatus != null || selectedMethod != null) {
                                    Text(
                                        text = "Limpiar filtros",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedStudent = null
                                                selectedStatus = null
                                                selectedMethod = null
                                            }
                                            .padding(vertical = 8.dp),
                                        color = WayraOrange,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Lista de pagos
                item {
                    Text(
                        text = "Resultados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (filteredPayments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSoft),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceCard
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron pagos",
                                    color = Ink,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredPayments) { payment ->
                        PaymentHistoryCard(
                            payment = payment,
                            student = allStudents.find { it.id == payment.studentId }
                        )
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDropdown(
    students: List<Student>,
    selectedStudent: Student?,
    onStudentSelected: (Student?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedStudent?.getFullName() ?: "Todos los alumnos",
            onValueChange = {},
            readOnly = true,
            label = { Text("Alumno") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los alumnos") },
                onClick = {
                    onStudentSelected(null)
                    expanded = false
                }
            )
            students.sortedBy { it.getFullName() }.forEach { student ->
                DropdownMenuItem(
                    text = { Text(student.getFullName()) },
                    onClick = {
                        onStudentSelected(student)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(
    selectedStatus: PaymentStatus?,
    onStatusSelected: (PaymentStatus?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedStatus?.let { getStatusName(it) } ?: "Todos los estados",
            onValueChange = {},
            readOnly = true,
            label = { Text("Estado") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los estados") },
                onClick = {
                    onStatusSelected(null)
                    expanded = false
                }
            )
            PaymentStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(getStatusName(status)) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodDropdown(
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedMethod?.let { getPaymentMethodName(it) } ?: "Todos los métodos",
            onValueChange = {},
            readOnly = true,
            label = { Text("Método de pago") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los métodos") },
                onClick = {
                    onMethodSelected(null)
                    expanded = false
                }
            )
            PaymentMethod.entries.forEach { method ->
                DropdownMenuItem(
                    text = { Text(getPaymentMethodName(method)) },
                    onClick = {
                        onMethodSelected(method)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentHistoryCard(
    payment: Payment,
    student: Student?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSoft),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lado izquierdo: Info del estudiante y pago
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del estudiante
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = getStatusColor(payment.status).copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student?.name?.firstOrNull()?.toString()?.uppercase() ?: "?",
                        fontFamily = AgenorNeue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = getStatusColor(payment.status)
                    )
                }

                // Información
                Column {
                    Text(
                        text = student?.getFullName() ?: "Alumno desconocido",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Ink
                    )
                    Text(
                        text = formatDate(payment.paymentDate ?: payment.dueDate ?: 0L),
                        fontSize = 12.sp,
                        color = Ink
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(status = payment.status)
                        Text(
                            text = "• ${getPaymentMethodName(payment.paymentMethod ?: PaymentMethod.EFECTIVO)}",
                            fontSize = 12.sp,
                            color = Ink
                        )
                    }
                }
            }

            // Lado derecho: Monto
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                    .format(payment.amount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WayraOrange
            )
        }
    }
}

@Composable
fun StatusChip(status: PaymentStatus) {
    Box(
        modifier = Modifier
            .background(
                color = getStatusColor(status).copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = getStatusName(status),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = getStatusColor(status)
        )
    }
}

fun getStatusName(status: PaymentStatus): String {
    return when (status) {
        PaymentStatus.PAGADO -> "Pagado"
        PaymentStatus.PENDIENTE -> "Pendiente"
        PaymentStatus.VENCIDO -> "Vencido"
    }
}

fun getStatusColor(status: PaymentStatus): Color {
    return when (status) {
        PaymentStatus.PAGADO -> Success
        PaymentStatus.PENDIENTE -> Warning
        PaymentStatus.VENCIDO -> Danger
    }
}
