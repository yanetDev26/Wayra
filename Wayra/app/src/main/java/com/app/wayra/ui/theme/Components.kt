package com.app.wayra.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

/**
 * Colores de la barra superior, centralizados para que todas las pantallas
 * usen exactamente el mismo encabezado.
 *
 * Se usa un encabezado claro en lugar de la banda naranja completa: la marca
 * aparece en los iconos, botones y acentos, y el resultado es más sobrio.
 * Además permite iconos oscuros en la barra de estado en toda la app, sin
 * depender de la pantalla.
 *
 * Para volver al encabezado naranja basta con cambiar los cuatro valores de
 * esta función:
 *   containerColor = WayraOrange, y los tres restantes = OnDark.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun wayraTopAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = Paper,
    scrolledContainerColor = Paper,
    titleContentColor = Ink,
    navigationIconContentColor = WayraOrangeDark,
    actionIconContentColor = WayraOrangeDark
)
