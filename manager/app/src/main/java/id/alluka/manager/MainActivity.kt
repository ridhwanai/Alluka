package id.alluka.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
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
                val pagerRoutes = listOf("home", "applist", "tweaks", "settings")
                val pagerState = rememberPagerState(initialPage = 0) { pagerRoutes.size }
                val scope = rememberCoroutineScope()

                // Android Emphasized Decelerate Animation Curve (AZenith Spec)
                val easeCurve = CubicBezierEasing(0.2f, 0f, 0f, 1f)

                val activeRoute = if (!pagerState.isScrollInProgress) {
                    pagerRoutes.getOrElse(pagerState.settledPage) { "home" }
                } else {
                    pagerRoutes.getOrElse(pagerState.targetPage) { "home" }
                }

                // Smooth back navigation to home page
                BackHandler(enabled = pagerState.currentPage != 0) {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            0,
                            animationSpec = tween(durationMillis = 380, easing = easeCurve)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 3 // Prefetch all 4 pages to eliminate all stutters
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen(
                                selectedNavRoute = activeRoute,
                                onRouteSelected = { route ->
                                    val target = pagerRoutes.indexOf(route).coerceAtLeast(0)
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            target,
                                            animationSpec = tween(
                                                durationMillis = if (kotlin.math.abs(target - pagerState.currentPage) > 1) 320 else 460,
                                                easing = easeCurve
                                            )
                                        )
                                    }
                                }
                            )
                            1 -> ApplistScreen()
                            2 -> TweakScreen()
                            3 -> SettingsScreen()
                        }
                    }

                    AllukaFloatingNavBar(
                        selectedRoute = activeRoute,
                        onRouteSelected = { route ->
                            val target = pagerRoutes.indexOf(route).coerceAtLeast(0)
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    target,
                                    animationSpec = tween(
                                        durationMillis = if (kotlin.math.abs(target - pagerState.currentPage) > 1) 320 else 460,
                                        easing = easeCurve
                                    )
                                )
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
