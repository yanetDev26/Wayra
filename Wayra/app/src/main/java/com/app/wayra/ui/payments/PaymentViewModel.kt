package com.app.wayra.ui.payments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wayra.data.model.Payment
import com.app.wayra.data.model.PaymentMethod
import com.app.wayra.data.model.PaymentStatus
import com.app.wayra.data.model.Student
import com.app.wayra.data.repository.PaymentRepository
import com.app.wayra.data.repository.StudentRepository
import com.app.wayra.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {

    private val studentRepository = StudentRepository()
    private val paymentRepository = PaymentRepository()
    private val subscriptionRepository = SubscriptionRepository()

    private val _students = MutableLiveData<List<Student>>().apply {
        value = emptyList()
    }
    val students: LiveData<List<Student>> = _students

    private val _selectedStudent = MutableLiveData<Student?>()
    val selectedStudent: LiveData<Student?> = _selectedStudent

    private val _amount = MutableLiveData<String>().apply {
        value = ""
    }
    val amount: LiveData<String> = _amount

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethod>().apply {
        value = PaymentMethod.EFECTIVO
    }
    val selectedPaymentMethod: LiveData<PaymentMethod> = _selectedPaymentMethod

    private val _notes = MutableLiveData<String>().apply {
        value = ""
    }
    val notes: LiveData<String> = _notes

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
                    _students.value = students
                }
        }
    }

    fun selectStudent(student: Student) {
        _selectedStudent.value = student
    }

    fun setAmount(value: String) {
        _amount.value = value
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun setNotes(value: String) {
        _notes.value = value
    }

    suspend fun registerPayment(): Boolean {
        val student = _selectedStudent.value ?: return false
        val amountValue = _amount.value?.toDoubleOrNull() ?: return false

        if (amountValue <= 0) return false

        // Obtener suscripción activa del estudiante
        val subscription = subscriptionRepository.getActiveSubscription(student.id)

        val payment = Payment(
            studentId = student.id,
            subscriptionId = subscription?.id ?: "",
            amount = amountValue,
            paymentDate = System.currentTimeMillis(),
            paymentMethod = _selectedPaymentMethod.value,
            status = PaymentStatus.PAGADO,
            notes = _notes.value ?: ""
        )

        val result = paymentRepository.addPayment(payment)
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
        _amount.value = ""
        _selectedPaymentMethod.value = PaymentMethod.EFECTIVO
        _notes.value = ""
    }
}
