package id.alluka.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.alluka.manager.ui.mainscreens.HomeScreen
import id.alluka.manager.ui.theme.AllukaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllukaTheme {
                HomeScreen()
            }
        }
    }
}
