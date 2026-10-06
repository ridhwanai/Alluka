package id.alluka.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.alluka.manager.ui.mainscreens.ApplistScreen
import id.alluka.manager.ui.mainscreens.HomeScreen
import id.alluka.manager.ui.mainscreens.SettingsScreen
import id.alluka.manager.ui.mainscreens.TweakScreen
import id.alluka.manager.ui.navigation.AllukaFloatingNavBar
import id.alluka.manager.ui.theme.AllukaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllukaTheme {
                val pagerState = rememberPagerState(initialPage = 0) { 4 }
                val scope = rememberCoroutineScope()

                val currentRoute = when (pagerState.currentPage) {
                    0 -> "home"
                    1 -> "applist"
                    2 -> "tweaks"
                    else -> "settings"
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen(
                                selectedNavRoute = currentRoute,
                                onRouteSelected = { route ->
                                    val target = when (route) {
                                        "home" -> 0
                                        "applist" -> 1
                                        "tweaks" -> 2
                                        "settings" -> 3
                                        else -> 0
                                    }
                                    scope.launch { pagerState.animateScrollToPage(target) }
                                }
                            )
                            1 -> ApplistScreen()
                            2 -> TweakScreen()
                            3 -> SettingsScreen()
                        }
                    }

                    AllukaFloatingNavBar(
                        selectedRoute = currentRoute,
                        onRouteSelected = { route ->
                            val target = when (route) {
                                "home" -> 0
                                "applist" -> 1
                                "tweaks" -> 2
                                "settings" -> 3
                                else -> 0
                            }
                            scope.launch {
                                pagerState.animateScrollToPage(target)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}
