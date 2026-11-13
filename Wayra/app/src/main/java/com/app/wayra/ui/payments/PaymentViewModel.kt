package com.app.wayra.ui.payments

import com.app.wayra.domain.AmountResult
import com.app.wayra.domain.PaymentRules
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.data.model.Plan
import com.app.wayra.data.model.Student
import com.app.wayra.data.repository.PaymentRepository
import com.app.wayra.data.repository.PlanRepository
import com.app.wayra.data.repository.StudentRepository
import com.app.wayra.data.repository.SubscriptionRepository
import com.app.wayra.domain.PaymentPeriod
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar

class PaymentViewModel : ViewModel() {

    private val studentRepository = StudentRepository()
    private val paymentRepository = PaymentRepository()
    private val subscriptionRepository = SubscriptionRepository()
    private val planRepository = PlanRepository()

    private val _students = MutableLiveData<List<Student>>().apply {
        value = emptyList()
    }
    val students: LiveData<List<Student>> = _students

    private val _selectedStudent = MutableLiveData<Student?>()
    val selectedStudent: LiveData<Student?> = _selectedStudent

    private val _selectedPlan = MutableLiveData<Plan?>()
    val selectedPlan: LiveData<Plan?> = _selectedPlan

    private val _amount = MutableLiveData<String>().apply {
        value = ""
    }
    val amount: LiveData<String> = _amount

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethod>().apply {
        value = PaymentMethod.EFECTIVO
    }
    val selectedPaymentMethod: LiveData<PaymentMethod> = _selectedPaymentMethod

    private val _selectedPaymentPeriod = MutableLiveData<PaymentPeriod>().apply {
        value = PaymentPeriod.CURRENT_MONTH
    }
    val selectedPaymentPeriod: LiveData<PaymentPeriod> = _selectedPaymentPeriod

    private val _notes = MutableLiveData<String>().apply {
        value = ""
    }
    val notes: LiveData<String> = _notes

    private val _currentPayment = MutableLiveData<Payment?>()
    val currentPayment: LiveData<Payment?> = _currentPayment

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            studentRepository.getActiveStudents()
                .catch { e ->
                    // Log error
                }
                .collect { students ->
                    // Ordenar alfabéticamente por nombre completo
                    _students.value = students.sortedBy { it.getFullName().lowercase() }
                }
        }
    }

    fun selectStudent(student: Student) {
        _selectedStudent.value = student
        // Cargar el plan asociado al estudiante automáticamente
        loadStudentPlan(student.id)
    }

    private fun loadStudentPlan(studentId: String) {
        viewModelScope.launch {
            try {
                // Obtener la suscripción activa del estudiante
                val subscription = subscriptionRepository.getActiveSubscription(studentId)

                if (subscription != null) {
                    // Obtener el plan asociado a la suscripción
                    val plan = planRepository.getPlanById(subscription.planId)

                    if (plan != null) {
                        _selectedPlan.value = plan
                        // Establecer automáticamente el monto del plan
                        _amount.value = plan.price.toString()
                    } else {
                        _selectedPlan.value = null
                        _amount.value = ""
                    }
                } else {
                    _selectedPlan.value = null
                    _amount.value = ""
                }
            } catch (_: Exception) {
                _selectedPlan.value = null
                _amount.value = ""
            }
        }
    }

    fun setAmount(value: String) {
        _amount.value = value
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun selectPaymentPeriod(period: PaymentPeriod) {
        _selectedPaymentPeriod.value = period
    }

    fun setNotes(value: String) {
        _notes.value = value
    }

    suspend fun registerPayment(): Boolean {
        val student = _selectedStudent.value ?: return false
        val amountValue = (PaymentRules.validateAmount(_amount.value.orEmpty()) as? AmountResult.Valid)?.value
            ?: return false

        // Obtener suscripción activa del estudiante
        val subscription = subscriptionRepository.getActiveSubscription(student.id)

        val now = System.currentTimeMillis()
        val dueDate = PaymentRules.dueDateFor(
            _selectedPaymentPeriod.value ?: PaymentPeriod.CURRENT_MONTH, now
        )

        // Buscar si existe un pago pendiente para este estudiante en este mes
        val existingPendingPayment = paymentRepository.findPendingPaymentForMonth(student.id, dueDate)

        val payment = PaymentRules.registeredPayment(
            existingPending = existingPendingPayment,
            studentId = student.id,
            subscriptionId = subscription?.id,
            amount = amountValue,
            method = _selectedPaymentMethod.value ?: PaymentMethod.EFECTIVO,
            notes = _notes.value.orEmpty(),
            dueDate = dueDate,
            now = now
        )

        // Si ya habia una cuota pendiente ese mes se salda; si no, se crea el pago.
        // Antes el resultado de la actualizacion se ignoraba y siempre se informaba exito.
        val result = if (existingPendingPayment != null) {
            paymentRepository.updatePayment(existingPendingPayment.id, payment)
        } else {
            paymentRepository.addPayment(payment)
        }

        return if (result.isSuccess) {
            clearForm()
            true
        } else {
            // Log error
            false
        }
    }

    private fun clearForm() {
        _selectedStudent.value = null
        _selectedPlan.value = null
        _amount.value = ""
        _selectedPaymentMethod.value = PaymentMethod.EFECTIVO
        _selectedPaymentPeriod.value = PaymentPeriod.CURRENT_MONTH
        _notes.value = ""
    }

    // Cargar pago específico para ver/editar
    suspend fun loadPayment(paymentId: String): Boolean {
        val payment = paymentRepository.getPaymentById(paymentId)
        return if (payment != null) {
            _currentPayment.value = payment
            _amount.value = payment.amount.toString()
            _selectedPaymentMethod.value = payment.paymentMethod ?: PaymentMethod.EFECTIVO
            _notes.value = payment.notes

            // Cargar el estudiante asociado
            _students.value?.find { it.id == payment.studentId }?.let { student ->
                _selectedStudent.value = student
            }
            true
        } else {
            false
        }
    }

    // Actualizar pago existente
    suspend fun updatePayment(paymentId: String): Result<Unit> {
        val amountValue = when (val r = PaymentRules.validateAmount(_amount.value.orEmpty())) {
            is AmountResult.Valid -> r.value
            else -> return Result.failure(Exception(r.errorMessage))
        }

        val currentPayment = _currentPayment.value ?: return Result.failure(Exception("No hay pago cargado"))

        val updatedPayment = currentPayment.copy(
            amount = amountValue,
            paymentMethod = _selectedPaymentMethod.value,
            notes = _notes.value ?: ""
        )

        return paymentRepository.updatePayment(paymentId, updatedPayment)
    }

    // Eliminar pago
    suspend fun deletePayment(paymentId: String): Result<Unit> {
        return paymentRepository.deletePayment(paymentId)
    }

    // Marcar pago como pagado
    suspend fun markAsPaid(paymentId: String): Result<Unit> {
        val payment = _currentPayment.value ?: return Result.failure(Exception("No hay pago cargado"))

        val updatedPayment = payment.copy(
            status = PaymentStatus.PAGADO,
            paymentDate = System.currentTimeMillis()
        )

        return paymentRepository.updatePayment(paymentId, updatedPayment)
    }

    // Marcar pago como pendiente
    suspend fun markAsPending(paymentId: String): Result<Unit> {
        val payment = _currentPayment.value ?: return Result.failure(Exception("No hay pago cargado"))

        val updatedPayment = payment.copy(
            status = PaymentStatus.PENDIENTE,
            paymentDate = null
        )

        return paymentRepository.updatePayment(paymentId, updatedPayment)
    }
}
