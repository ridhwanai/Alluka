package id.alluka.manager.ui.component

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.LruCache
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppIconCache {
    private val cache = LruCache<String, ImageBitmap>(150)

    fun get(packageName: String): ImageBitmap? = synchronized(cache) { cache.get(packageName) }

    fun clear() = synchronized(cache) { cache.evictAll() }

    suspend fun loadIcon(pm: PackageManager, appInfo: ApplicationInfo, targetSizePx: Int): ImageBitmap =
        withContext(Dispatchers.IO) {
            get(appInfo.packageName)?.let { return@withContext it }

            val drawable = try {
                appInfo.loadIcon(pm) ?: pm.defaultActivityIcon
            } catch (_: Exception) {
                pm.defaultActivityIcon
            }

            val bitmap = Bitmap.createBitmap(targetSizePx, targetSizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            drawable.setBounds(0, 0, targetSizePx, targetSizePx)
            drawable.draw(canvas)

            val imageBitmap = bitmap.asImageBitmap()

            synchronized(cache) {
                cache.put(appInfo.packageName, imageBitmap)
            }

            imageBitmap
        }
}

@Composable
fun AppIconImage(
    packageName: String,
    appInfo: ApplicationInfo?,
    appName: String,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val density = LocalDensity.current
    val targetSizePx = remember(size, density) {
        with(density) { size.roundToPx() }
    }

    var appBitmap by remember(packageName) {
        mutableStateOf(AppIconCache.get(packageName))
    }

    LaunchedEffect(packageName, targetSizePx) {
        if (appBitmap == null && appInfo != null) {
            try {
                appBitmap = AppIconCache.loadIcon(pm, appInfo, targetSizePx)
            } catch (_: Exception) {}
        }
    }

    Box(modifier = modifier.size(size)) {
        Crossfade(
            targetState = appBitmap,
            animationSpec = tween(durationMillis = 200),
            label = "AppIconFade"
        ) { icon ->
            if (icon == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                )
            } else {
                Image(
                    bitmap = icon,
                    contentDescription = appName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}
