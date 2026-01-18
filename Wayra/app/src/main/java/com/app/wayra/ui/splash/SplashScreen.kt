package com.app.wayra.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.wayra.R
import com.app.wayra.ui.components.WayraBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Animación de escala con efecto suave
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Animación de opacidad (fade in)
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    // Lanzar animación y navegar después de 2 segundos
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        onSplashFinished()
    }

    // UI del Splash Screen
    WayraBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_wayra),
                contentDescription = "Logo Wayra",
                modifier = Modifier
                    .size(220.dp)
                    .scale(scale)
                    .alpha(alpha)
            )
        }
    }
}
