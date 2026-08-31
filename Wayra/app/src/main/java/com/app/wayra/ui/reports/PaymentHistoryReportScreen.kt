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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme

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
                            tint = OnBrand
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
                val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                val activeFilters = listOfNotNull(selectedStudent, selectedStatus, selectedMethod).size

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
                ) {
                    item {
                        // La cifra que importa es cuanto suma lo filtrado; el
                        // recuento pasa a la tira de metricas.
                        ReportHero(
                            caption = "Total filtrado",
                            value = currency.format(filteredPayments.sumOf { it.amount }),
                            note = if (activeFilters > 0)
                                "$activeFilters ${if (activeFilters == 1) "filtro activo" else "filtros activos"}"
                            else "Sin filtros"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ReportStatStrip(
                            stats = listOf(
                                ReportStat(
                                    if (filteredPayments.size == 1) "Pago" else "Pagos",
                                    "${filteredPayments.size}"
                                ),
                                ReportStat(
                                    "Pagados",
                                    "${filteredPayments.count { it.status == PaymentStatus.PAGADO }}",
                                    Tone.Success
                                )
                            )
                        )
                    }

                    if (showFilters) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionLabel(text = "Filtros")
                                if (activeFilters > 0) {
                                    Text(
                                        text = "Limpiar",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = WayraOrangeDark,
                                        modifier = Modifier
                                            .clickable {
                                                selectedStudent = null
                                                selectedStatus = null
                                                selectedMethod = null
                                            }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            WayraPanel(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    StudentDropdown(
                                        students = allStudents,
                                        selectedStudent = selectedStudent,
                                        onStudentSelected = { selectedStudent = it }
                                    )
                                    StatusDropdown(
                                        selectedStatus = selectedStatus,
                                        onStatusSelected = { selectedStatus = it }
                                    )
                                    PaymentMethodDropdown(
                                        selectedMethod = selectedMethod,
                                        onMethodSelected = { selectedMethod = it }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionLabel(text = "Resultados")
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (filteredPayments.isEmpty()) {
                        item {
                            ReportEmpty(
                                text = if (activeFilters > 0)
                                    "Ningún pago coincide con los filtros."
                                else "Todavía no hay pagos registrados."
                            )
                        }
                    } else {
                        itemsIndexed(filteredPayments) { index, payment ->
                            val student = allStudents.find { it.id == payment.studentId }
                            ReportRowItem(
                                title = student?.getFullName() ?: "Alumno no encontrado",
                                subtitle = formatDate(payment.paymentDate ?: payment.dueDate ?: 0L),
                                amount = currency.format(payment.amount),
                                badgeText = getStatusName(payment.status),
                                badgeTone = when (payment.status) {
                                    PaymentStatus.PAGADO -> Tone.Success
                                    PaymentStatus.PENDIENTE -> Tone.Warning
                                    PaymentStatus.VENCIDO -> Tone.Danger
                                },
                                isFirst = index == 0,
                                isLast = index == filteredPayments.lastIndex
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

fun getStatusName(status: PaymentStatus): String {
    return when (status) {
        PaymentStatus.PAGADO -> "Pagado"
        PaymentStatus.PENDIENTE -> "Pendiente"
        PaymentStatus.VENCIDO -> "Vencido"
    }
}

@Composable
@ReadOnlyComposable
fun getStatusColor(status: PaymentStatus): Color {
    return when (status) {
        PaymentStatus.PAGADO -> Success
        PaymentStatus.PENDIENTE -> Warning
        PaymentStatus.VENCIDO -> Danger
    }
}
