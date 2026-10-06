package id.alluka.manager.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Shared opacity for all skeleton blocks in the subtree.
 */
val LocalSkeletonAlpha = compositionLocalOf { 1f }

/**
 * Android 16 / AZenith Expressive Shimmer container.
 * Synchronizes the alpha pulse across all placeholder elements.
 */
@Composable
fun SkeletonContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "AllukaSkeletonShimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.58f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AllukaSkeletonShimmerAlpha"
    )
    CompositionLocalProvider(LocalSkeletonAlpha provides alpha) {
        Box(modifier = modifier) { content() }
    }
}

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val alpha = LocalSkeletonAlpha.current
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha))
    )
}

@Composable
fun SkeletonCircle(modifier: Modifier = Modifier) {
    val alpha = LocalSkeletonAlpha.current
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha))
    )
}

@Composable
fun SkeletonListRow(
    modifier: Modifier = Modifier,
    circleSize: Int = 42
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SkeletonCircle(modifier = Modifier.size(circleSize.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(15.dp)
            )
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.38f)
                    .height(11.dp)
            )
        }
        SkeletonBlock(
            modifier = Modifier
                .width(44.dp)
                .height(24.dp),
            shape = RoundedCornerShape(20.dp)
        )
    }
}
