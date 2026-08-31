package com.app.wayra.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.wayra.ui.theme.Danger
import com.app.wayra.ui.theme.OnBrand
import com.app.wayra.ui.theme.Success

enum class SnackbarType {
    SUCCESS,
    ERROR
}

@Composable
fun CustomSnackbarHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { data ->
            // Determinar el tipo basado en un prefijo en el mensaje
            val message = data.visuals.message
            val (type, displayMessage) = when {
                message.startsWith("SUCCESS:") -> SnackbarType.SUCCESS to message.removePrefix("SUCCESS:")
                message.startsWith("ERROR:") -> SnackbarType.ERROR to message.removePrefix("ERROR:")
                else -> SnackbarType.SUCCESS to message
            }

            val backgroundColor = when (type) {
                SnackbarType.SUCCESS -> Success // Verde
                SnackbarType.ERROR -> Danger   // Rojo
            }

            Snackbar(
                containerColor = backgroundColor,
                contentColor = OnBrand,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = displayMessage.trim())
            }
        }
    )
}

// Funciones auxiliares para mostrar snackbars
suspend fun SnackbarHostState.showSuccessSnackbar(message: String) {
    showSnackbar("SUCCESS:$message")
}

suspend fun SnackbarHostState.showErrorSnackbar(message: String) {
    showSnackbar("ERROR:$message")
}
