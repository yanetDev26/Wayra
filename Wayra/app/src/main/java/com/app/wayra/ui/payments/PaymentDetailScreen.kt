package com.app.wayra.ui.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    paymentId: String,
    onNavigateBack: () -> Unit,
    viewModel: PaymentViewModel = viewModel()
) {
    val currentPayment by viewModel.currentPayment.observeAsState()
    val selectedStudent by viewModel.selectedStudent.observeAsState()
    val amount by viewModel.amount.observeAsState("")
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.observeAsState(PaymentMethod.EFECTIVO)
    val notes by viewModel.notes.observeAsState("")

    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPaymentMethodDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar el pago al iniciar
    LaunchedEffect(paymentId) {
        val loaded = viewModel.loadPayment(paymentId)
        if (!loaded) {
            snackbarHostState.showSnackbar("Error al cargar el pago")
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Pago") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (isEditMode) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = viewModel.updatePayment(paymentId)
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar("Pago actualizado")
                                        isEditMode = false
                                    } else {
                                        snackbarHostState.showSnackbar("Error al actualizar")
                                    }
                                }
                            }
                        ) {
                            Text("Guardar")
                        }
                        TextButton(onClick = { isEditMode = false }) {
                            Text("Cancelar")
                        }
                    } else {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Eliminar")
                        }
                    }
                }
            )
        },
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            currentPayment?.let { payment ->
                // Card de información del pago
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información del Pago",
                            fontFamily = AgenorNeue,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        // Estado del pago
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estado:",
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(
                                text = payment.status.name,
                                tone = when (payment.status) {
                                    PaymentStatus.PAGADO -> Tone.Success
                                    PaymentStatus.PENDIENTE -> Tone.Warning
                                    PaymentStatus.VENCIDO -> Tone.Danger
                                }
                            )
                        }

                        // Botones para cambiar estado
                        if (!isEditMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (payment.status != PaymentStatus.PAGADO) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val result = viewModel.markAsPaid(paymentId)
                                                if (result.isSuccess) {
                                                    snackbarHostState.showSnackbar("Marcado como pagado")
                                                    viewModel.loadPayment(paymentId)
                                                } else {
                                                    snackbarHostState.showSnackbar("Error al actualizar")
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Success
                                        )
                                    ) {
                                        Text("Marcar como Pagado")
                                    }
                                }
                                if (payment.status != PaymentStatus.PENDIENTE) {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val result = viewModel.markAsPending(paymentId)
                                                if (result.isSuccess) {
                                                    snackbarHostState.showSnackbar("Marcado como pendiente")
                                                    viewModel.loadPayment(paymentId)
                                                } else {
                                                    snackbarHostState.showSnackbar("Error al actualizar")
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Marcar como Pendiente")
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        // Estudiante
                        DetailRow(
                            label = "Alumno:",
                            value = selectedStudent?.getFullName() ?: "Cargando..."
                        )

                        // Monto
                        if (isEditMode) {
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { viewModel.setAmount(it) },
                                label = { Text("Monto") },
                                prefix = { Text("$") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            DetailRow(
                                label = "Monto:",
                                value = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
                                    .format(payment.amount)
                            )
                        }

                        // Método de pago
                        if (isEditMode) {
                            OutlinedButton(
                                onClick = { showPaymentMethodDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Método: ${selectedPaymentMethod.name}")
                            }
                        } else {
                            DetailRow(
                                label = "Método de pago:",
                                value = payment.paymentMethod?.name ?: "No especificado"
                            )
                        }

                        // Fecha de vencimiento
                        payment.dueDate?.let { dueDate ->
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            DetailRow(
                                label = "Fecha de vencimiento:",
                                value = dateFormat.format(Date(dueDate))
                            )
                        }

                        // Fecha de pago
                        payment.paymentDate?.let { paymentDate ->
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            DetailRow(
                                label = "Fecha de pago:",
                                value = dateFormat.format(Date(paymentDate))
                            )
                        }

                        // Notas
                        if (isEditMode) {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { viewModel.setNotes(it) },
                                label = { Text("Notas") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )
                        } else if (payment.notes.isNotEmpty()) {
                            Column {
                                Text(
                                    text = "Notas:",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = payment.notes)
                            }
                        }
                    }
                }
            }
        }
        }
    }

    // Diálogo de método de pago
    if (showPaymentMethodDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentMethodDialog = false },
            title = { Text("Seleccionar Método de Pago") },
            text = {
                Column {
                    PaymentMethod.entries.forEach { method ->
                        TextButton(
                            onClick = {
                                viewModel.selectPaymentMethod(method)
                                showPaymentMethodDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(method.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentMethodDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Pago") },
            text = { Text("¿Estás seguro de que deseas eliminar este pago?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            val result = viewModel.deletePayment(paymentId)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Pago eliminado")
                                onNavigateBack()
                            } else {
                                snackbarHostState.showSnackbar("Error al eliminar")
                            }
                        }
                    }
                ) {
                    Text("Eliminar", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.5f)
        )
    }
}
