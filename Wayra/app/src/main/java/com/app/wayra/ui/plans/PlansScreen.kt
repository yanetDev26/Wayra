package com.app.wayra.ui.plans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.wayra.R
import com.app.wayra.data.model.Plan
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    viewModel: PlansViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddPlan: () -> Unit
) {
    val plans by viewModel.plans.observeAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<Plan?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.plans_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddPlan) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.plans_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Botón para cargar planes iniciales (solo se muestra si no hay planes)
            if (plans.isEmpty()) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.loadInitialPlans()
                            snackbarHostState.showSnackbar(
                                message = "Planes iniciales cargados correctamente",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(stringResource(R.string.load_initial_data))
                }
            }

            if (plans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.plans_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plans) { plan ->
                        PlanItem(
                            plan = plan,
                            onEdit = {
                                selectedPlan = plan
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedPlan = plan
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Diálogo de editar
        if (showEditDialog && selectedPlan != null) {
            EditPlanDialog(
                plan = selectedPlan!!,
                onDismiss = { showEditDialog = false },
                onSave = { updatedPlan ->
                    coroutineScope.launch {
                        val result = viewModel.updatePlan(selectedPlan!!.id, updatedPlan)
                        if (result.isSuccess) {
                            viewModel.refreshData()
                            snackbarHostState.showSnackbar("Plan actualizado")
                            showEditDialog = false
                        } else {
                            snackbarHostState.showSnackbar("Error al actualizar")
                        }
                    }
                }
            )
        }

        // Diálogo de eliminar
        if (showDeleteDialog && selectedPlan != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar Plan") },
                text = { Text("¿Estás seguro de que deseas eliminar ${selectedPlan!!.activityName}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                val result = viewModel.deletePlan(selectedPlan!!.id)
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar("Plan eliminado")
                                    showDeleteDialog = false
                                } else {
                                    snackbarHostState.showSnackbar("Error al eliminar")
                                }
                            }
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
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
}

@Composable
fun PlanItem(
    plan: Plan,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.activityName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (plan.description.isNotBlank()) {
                        Text(
                            text = plan.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(plan.price),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditPlanDialog(
    plan: Plan,
    onDismiss: () -> Unit,
    onSave: (Plan) -> Unit
) {
    var activityName by remember { mutableStateOf(plan.activityName) }
    var description by remember { mutableStateOf(plan.description) }
    var price by remember { mutableStateOf(plan.price.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    label = { Text("Nombre del Plan") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    singleLine = false,
                    maxLines = 3
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priceValue = price.toDoubleOrNull() ?: plan.price
                    onSave(
                        plan.copy(
                            activityName = activityName,
                            description = description,
                            price = priceValue
                        )
                    )
                },
                enabled = activityName.isNotBlank() && price.toDoubleOrNull() != null
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
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
    return format.format(amount)
}
