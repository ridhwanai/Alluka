package id.alluka.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.alluka.manager.ui.mainscreens.ApplistScreen
import id.alluka.manager.ui.mainscreens.HomeScreen
import id.alluka.manager.ui.mainscreens.SettingsScreen
import id.alluka.manager.ui.navigation.AllukaFloatingNavBar
import id.alluka.manager.ui.theme.AllukaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllukaTheme {
                var currentRoute by remember { mutableStateOf("home") }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentRoute) {
                        "home" -> HomeScreen(
                            selectedNavRoute = currentRoute,
                            onRouteSelected = { currentRoute = it }
                        )
                        "applist" -> ApplistScreen()
                        "tweaks" -> ApplistScreen()
                        "settings" -> SettingsScreen()
                    }

                    if (currentRoute != "home") {
                        AllukaFloatingNavBar(
                            selectedRoute = currentRoute,
                            onRouteSelected = { currentRoute = it },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
