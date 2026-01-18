package com.app.wayra.ui.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.unit.dp
import com.app.wayra.R
import com.app.wayra.data.model.PaymentMethod
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
    val notes by viewModel.notes.observeAsState("")

    val coroutineScope = rememberCoroutineScope()
    var showStudentPicker by remember { mutableStateOf(false) }
    var showPaymentMethodPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_payment_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                                "El estudiante no tiene un plan asignado"
                            } else {
                                "Selecciona un estudiante para ver el plan"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
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

            Spacer(modifier = Modifier.weight(1f))

            // Register Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true

                        // Validar datos antes de intentar registrar
                        if (selectedStudent == null) {
                            snackbarHostState.showSnackbar("Por favor selecciona un estudiante")
                            isLoading = false
                            return@launch
                        }

                        if (amount.isBlank()) {
                            snackbarHostState.showSnackbar("Por favor ingresa un monto")
                            isLoading = false
                            return@launch
                        }

                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null) {
                            snackbarHostState.showSnackbar("El monto debe ser un número válido")
                            isLoading = false
                            return@launch
                        }

                        if (amountValue <= 0) {
                            snackbarHostState.showSnackbar("El monto debe ser mayor a 0")
                            isLoading = false
                            return@launch
                        }

                        // Intentar registrar el pago
                        val success = viewModel.registerPayment()
                        isLoading = false

                        if (success) {
                            snackbarHostState.showSnackbar("Pago registrado exitosamente")
                            onPaymentRegistered()
                        } else {
                            snackbarHostState.showSnackbar("Error al registrar el pago. Verifica que el estudiante tenga una suscripción activa.")
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

    // Student Picker Dialog
    if (showStudentPicker) {
        AlertDialog(
            onDismissRequest = { showStudentPicker = false },
            title = { Text(stringResource(R.string.payment_select_student)) },
            text = {
                Column {
                    students.forEach { student ->
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
}

@Composable
fun getPaymentMethodLabel(method: PaymentMethod): String {
    return when (method) {
        PaymentMethod.EFECTIVO -> stringResource(R.string.payment_cash)
        PaymentMethod.TRANSFERENCIA -> stringResource(R.string.payment_transfer)
    }
}
