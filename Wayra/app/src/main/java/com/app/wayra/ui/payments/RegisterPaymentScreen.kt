package com.app.wayra.ui.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.ui.components.CustomSnackbarHost
import com.app.wayra.ui.components.showErrorSnackbar
import com.app.wayra.ui.components.showSuccessSnackbar
import com.app.wayra.ui.home.PaymentPeriod
import com.app.wayra.ui.theme.Ink
import com.app.wayra.ui.theme.InkMuted
import com.app.wayra.ui.theme.OnBrand
import com.app.wayra.ui.theme.Paper
import com.app.wayra.ui.theme.SurfaceAlt
import com.app.wayra.ui.theme.WayraOrange
import com.app.wayra.ui.theme.wayraTopAppBarColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(
    viewModel: PaymentViewModel,
    onNavigateBack: () -> Unit,
    onPaymentRegistered: () -> Unit
) {
    val students by viewModel.students.observeAsState(emptyList())
    val selectedStudent by viewModel.selectedStudent.observeAsState()
    val selectedPlan by viewModel.selectedPlan.observeAsState()
    val amount by viewModel.amount.observeAsState("")
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.observeAsState(PaymentMethod.EFECTIVO)
    val selectedPaymentPeriod by viewModel.selectedPaymentPeriod.observeAsState(PaymentPeriod.CURRENT_MONTH)
    val notes by viewModel.notes.observeAsState("")

    val coroutineScope = rememberCoroutineScope()
    var showStudentPicker by remember { mutableStateOf(false) }
    var showPaymentMethodPicker by remember { mutableStateOf(false) }
    var showPaymentPeriodPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_payment_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = wayraTopAppBarColors()
            )
        },
        snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
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
                            text = stringResource(R.string.payment_student),
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

            // Plan y Monto (solo lectura, se carga automáticamente)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.payment_plan_and_amount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedPlan != null) {
                        Text(
                            text = selectedPlan!!.activityName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "$ $amount",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = if (selectedStudent != null) {
                                "El alumno no tiene un plan asignado"
                            } else {
                                "Selecciona un alumno para ver el plan"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Payment Period Selector
            OutlinedCard(
                onClick = { showPaymentPeriodPicker = true },
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
                            text = "Corresponde al mes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (selectedPaymentPeriod) {
                                PaymentPeriod.LAST_MONTH -> "Mes Anterior"
                                PaymentPeriod.CURRENT_MONTH -> "Mes Actual"
                                PaymentPeriod.NEXT_MONTH -> "Mes Próximo"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            // Payment Method Selector
            OutlinedCard(
                onClick = { showPaymentMethodPicker = true },
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
                            text = stringResource(R.string.payment_method),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = getPaymentMethodLabel(selectedPaymentMethod),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.setNotes(it) },
                label = { Text(stringResource(R.string.payment_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Register Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true

                        // Validar datos antes de intentar registrar
                        if (selectedStudent == null) {
                            snackbarHostState.showErrorSnackbar("Por favor selecciona un alumno")
                            isLoading = false
                            return@launch
                        }

                        if (amount.isBlank()) {
                            snackbarHostState.showErrorSnackbar("Por favor ingresa un monto")
                            isLoading = false
                            return@launch
                        }

                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null) {
                            snackbarHostState.showErrorSnackbar("El monto debe ser un número válido")
                            isLoading = false
                            return@launch
                        }

                        if (amountValue <= 0) {
                            snackbarHostState.showErrorSnackbar("El monto debe ser mayor a 0")
                            isLoading = false
                            return@launch
                        }

                        // Intentar registrar el pago
                        val success = viewModel.registerPayment()
                        isLoading = false

                        if (success) {
                            snackbarHostState.showSuccessSnackbar("Pago registrado exitosamente")
                            onPaymentRegistered()
                        } else {
                            snackbarHostState.showErrorSnackbar("Error al registrar el pago. Verifica que el alumno tenga una suscripción activa.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && selectedStudent != null && selectedPlan != null && amount.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.payment_register))
                }
            }
            }
        }
    }

    // Student Picker Dialog
    if (showStudentPicker) {
        AlertDialog(
            onDismissRequest = { showStudentPicker = false },
            title = { Text(stringResource(R.string.payment_select_student)) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(students) { student ->
                        TextButton(
                            onClick = {
                                viewModel.selectStudent(student)
                                showStudentPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(student.getFullName(), modifier = Modifier.fillMaxWidth())
                        }
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

    // Payment Method Picker Dialog
    if (showPaymentMethodPicker) {
        AlertDialog(
            onDismissRequest = { showPaymentMethodPicker = false },
            title = { Text(stringResource(R.string.payment_method)) },
            text = {
                Column {
                    PaymentMethod.entries.forEach { method ->
                        TextButton(
                            onClick = {
                                viewModel.selectPaymentMethod(method)
                                showPaymentMethodPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(getPaymentMethodLabel(method), modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentMethodPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Payment Period Picker Dialog
    if (showPaymentPeriodPicker) {
        AlertDialog(
            onDismissRequest = { showPaymentPeriodPicker = false },
            title = { Text("Seleccionar período") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentPeriod.entries.forEach { period ->
                        val isSelected = selectedPaymentPeriod == period
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectPaymentPeriod(period)
                                    showPaymentPeriodPicker = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) WayraOrange else SurfaceAlt
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 4.dp else 1.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = when (period) {
                                        PaymentPeriod.LAST_MONTH -> "Mes Anterior"
                                        PaymentPeriod.CURRENT_MONTH -> "Mes Actual"
                                        PaymentPeriod.NEXT_MONTH -> "Mes Próximo"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Paper else Ink
                                )
                                Text(
                                    text = when (period) {
                                        PaymentPeriod.LAST_MONTH -> "Pago atrasado del mes pasado"
                                        PaymentPeriod.CURRENT_MONTH -> "Pago del mes actual (por defecto)"
                                        PaymentPeriod.NEXT_MONTH -> "Pago adelantado del próximo mes"
                                    },
                                    fontSize = 12.sp,
                                    color = if (isSelected) OnBrand.copy(alpha = 0.8f) else InkMuted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentPeriodPicker = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun getPaymentMethodLabel(method: PaymentMethod): String {
    return when (method) {
        PaymentMethod.EFECTIVO -> stringResource(R.string.payment_cash)
        PaymentMethod.TRANSFERENCIA -> stringResource(R.string.payment_transfer)
    }
}
