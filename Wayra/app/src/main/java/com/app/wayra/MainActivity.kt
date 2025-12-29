package com.app.wayra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import com.app.wayra.navigation.WayraNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Inicio", "Dashboard", "Notificaciones")

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = when (index) {
                                        0 -> R.drawable.home
                                        1 -> R.drawable.group_running
                                        else -> R.drawable.payment
                                    }
                                ),
                                contentDescription = item
                            )
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedItem) {
            0 -> {
                val navController = rememberNavController()
                WayraNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            1 -> {
                // Placeholder para Dashboard - necesitarás pasar el ViewModel apropiadamente
                Text("Dashboard", modifier = Modifier.padding(innerPadding))
            }
            2 -> {
                // Placeholder para Notifications - necesitarás pasar el ViewModel apropiadamente
                Text("Notificaciones", modifier = Modifier.padding(innerPadding))
            }
        }
    }
}