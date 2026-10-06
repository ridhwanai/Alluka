package id.alluka.manager.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import id.alluka.manager.ui.component.AllukaProfile

// Mode Daily: Sakura / Rose Warm
val DailyColorScheme = darkColorScheme(
    primary = Color(0xFFFF6584),
    onPrimary = Color(0xFF4A0019),
    primaryContainer = Color(0xFF670026),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = Color(0xFFFF8FA3),
    onSecondary = Color(0xFF561D2B),
    background = Color(0xFF160E18),
    surface = Color(0xFF20141E),
    surfaceVariant = Color(0xFF321C2C),
    onSurface = Color(0xFFFDF2F4)
)

// Mode Sleep: Midnight Indigo / Lunar Violet
val SleepColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFA5B4FC),
    onSecondary = Color(0xFF282860),
    background = Color(0xFF0A0D1A),
    surface = Color(0xFF111827),
    surfaceVariant = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9)
)

// Mode Peforma: Neon Crimson & Violet (Nanika)
val PeformaColorScheme = darkColorScheme(
    primary = Color(0xFFC026D3),
    onPrimary = Color(0xFF4A044E),
    primaryContainer = Color(0xFF701A75),
    onPrimaryContainer = Color(0xFFFDF4FF),
    secondary = Color(0xFFF43F5E),
    onSecondary = Color(0xFF4C0519),
    background = Color(0xFF16061C),
    surface = Color(0xFF200A26),
    surfaceVariant = Color(0xFF320F3C),
    onSurface = Color(0xFFFFF1F2)
)

@Composable
fun AllukaTheme(
    profile: AllukaProfile = AllukaProfile.DAILY,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (profile) {
        AllukaProfile.SLEEP -> SleepColorScheme
        AllukaProfile.DAILY -> DailyColorScheme
        AllukaProfile.PEFORMA -> PeformaColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
