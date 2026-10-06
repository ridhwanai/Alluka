package id.alluka.manager.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.alluka.manager.R

enum class AllukaProfile(
    val id: String,
    val title: String,
    val subtitle: String,
    val bannerRes: Int
) {
    SLEEP(
        "sleep",
        "Sleep Mode",
        "Hemat Daya Maksimal",
        R.drawable.alluka_sleep
    ),
    DAILY(
        "daily",
        "Daily Mode",
        "Smooth & Nyaman Harian",
        R.drawable.alluka_daily
    ),
    PEFORMA(
        "peforma",
        "Peforma Mode",
        "Kekuatan Gaming Penuh",
        R.drawable.nanika_peforma
    )
}

@Composable
fun AllukaDynamicBanner(
    currentProfile: AllukaProfile,
    pid: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(165.dp)
            .clip(RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic Crossfade Transition on Mode Change
            AnimatedContent(
                targetState = currentProfile,
                transitionSpec = {
                    (fadeIn(tween(450)) + scaleIn(tween(450), initialScale = 1.05f))
                        .togetherWith(fadeOut(tween(300)))
                },
                label = "AllukaBannerArtworkSwap"
            ) { profile ->
                Image(
                    painter = painterResource(id = profile.bannerRes),
                    contentDescription = profile.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Glassmorphic Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.05f),
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Top Badges: PID Pill on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.58f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = "PID: $pid",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bottom Profile Content: Mode Pill Persis AZenith (Tanpa Kata Aktif & Tanpa Quote)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Surface(
                    color = Color(0xCC140F1A),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.08f)
                    ),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = currentProfile.title,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
