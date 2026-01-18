package com.app.wayra.ui.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.wayra.R
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.ui.plans.PlansViewModel
import com.app.wayra.ui.subscriptions.SubscriptionViewModel
import com.app.wayra.ui.theme.WayraOrange
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val plans by plansViewModel.plans.observeAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    // Función para validar email
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Función para validar número de celular argentino
    fun isValidPhone(phone: String): Boolean {
        // Celulares argentinos: 10 dígitos (código de área + número)
        val digits = phone.replace(Regex("[^0-9]"), "").length
        return digits == 10
    }

    // Función para formatear el número con espacios (sin el 9 inicial)
    fun formatPhone(phone: String): String {
        return when {
            phone.isEmpty() -> ""
            phone.length <= 4 -> phone
            phone.length <= 10 -> {
                val part1 = phone.substring(0, minOf(4, phone.length))
                val part2 = phone.substring(4)
                "$part1${if (part2.isNotEmpty()) " $part2" else ""}"
            }
            else -> phone
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.students_add)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WayraOrange,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
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
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = email.isNotBlank() && !isValidEmail(email)
                },
                label = { Text(stringResource(R.string.student_email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                isError = emailError,
                supportingText = {
                    if (emailError) {
                        Text("Formato de email inválido")
                    }
                }
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    // Solo permitir números y limitar a 10 dígitos
                    val filtered = newValue.filter { it.isDigit() }.take(10)
                    phone = filtered
                    phoneError = phone.isNotBlank() && !isValidPhone(phone)
                },
                label = { Text(stringResource(R.string.student_phone)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                visualTransformation = { text ->
                    val prefix = "+54 9 "
                    val formatted = formatPhone(text.text)
                    TransformedText(
                        AnnotatedString(prefix + formatted),
                        object : OffsetMapping {
                            override fun originalToTransformed(offset: Int): Int {
                                // Calcular el offset considerando los espacios agregados
                                val beforeCursor = text.text.substring(0, minOf(offset, text.text.length))
                                val formattedBeforeCursor = formatPhone(beforeCursor)
                                return prefix.length + formattedBeforeCursor.length
                            }
                            override fun transformedToOriginal(offset: Int): Int {
                                if (offset <= prefix.length) return 0
                                val textPart = (offset - prefix.length).coerceAtLeast(0)
                                return text.text.take(textPart).count { it.isDigit() }
                            }
                        }
                    )
                },
                isError = phoneError,
                supportingText = {
                    if (phoneError) {
                        Text("Debe tener 10 dígitos (ej: 3492 566468)")
                    } else {
                        Text(
                            "Código de área + número (10 dígitos)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                        // Capturar el plan en una variable local para el smart cast
                        val plan = selectedPlan
                        if (name.isNotBlank() && surname.isNotBlank() &&
                            email.isNotBlank() && isValidEmail(email) &&
                            phone.isNotBlank() && isValidPhone(phone) &&
                            plan != null) {
                            coroutineScope.launch {
                                isLoading = true

                                // Agregar prefijo +54 9 al teléfono
                                val fullPhone = "+549$phone"

                                val student = Student(
                                    name = name,
                                    surname = surname,
                                    email = email,
                                    phone = fullPhone,
                                    registrationDate = Timestamp.now(),
                                    active = isActive
                                )
                                val result = viewModel.addStudent(student)
                                if (result.isSuccess) {
                                    // Crear la suscripción con el plan seleccionado
                                    val studentId = result.getOrNull()
                                    if (studentId != null) {
                                        // assignPlan creará automáticamente la suscripción y el pago pendiente
                                        subscriptionViewModel.assignPlan(
                                            student.copy(id = studentId),
                                            plan
                                        )
                                    }
                                    isLoading = false
                                    onStudentAdded()
                                } else {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && name.isNotBlank() &&
                            surname.isNotBlank() &&
                            email.isNotBlank() && !emailError && isValidEmail(email) &&
                            phone.isNotBlank() && !phoneError && isValidPhone(phone) &&
                            selectedPlan != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = androidx.compose.ui.graphics.Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.save))
                    }
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
