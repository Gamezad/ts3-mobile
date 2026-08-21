package io.github.ts3mobile.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ColdTs Client — icy blue palette. A pale glacier surface in light mode and a
// deep arctic navy in dark mode, all anchored by a crisp cyan-blue primary.
private val IcyLight = lightColorScheme(
    primary = Color(0xFF0B6FA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE8FB),
    onPrimaryContainer = Color(0xFF001E2F),
    secondary = Color(0xFF3F6880),
    secondaryContainer = Color(0xFFD2E8F5),
    onSecondaryContainer = Color(0xFF081F2C),
    tertiary = Color(0xFF5A5B7E),
    tertiaryContainer = Color(0xFFDFE0FF),
    background = Color(0xFFF4F9FD),
    onBackground = Color(0xFF0F1720),
    surface = Color(0xFFF8FBFE),
    onSurface = Color(0xFF0F1720),
    surfaceVariant = Color(0xFFDCE7F0),
    onSurfaceVariant = Color(0xFF405260),
    surfaceTint = Color(0xFF0B6FA8),
    outline = Color(0xFF708290),
    outlineVariant = Color(0xFFC0D0DC),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val IcyDark = darkColorScheme(
    primary = Color(0xFF7CD0FF),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF0B4E77),
    onPrimaryContainer = Color(0xFFCDE8FB),
    secondary = Color(0xFFA7CCE3),
    secondaryContainer = Color(0xFF294E64),
    onSecondaryContainer = Color(0xFFE0F1FB),
    tertiary = Color(0xFFC3C3EA),
    tertiaryContainer = Color(0xFF44456A),
    background = Color(0xFF080F17),
    onBackground = Color(0xFFE2EDF5),
    surface = Color(0xFF0D1722),
    onSurface = Color(0xFFE2EDF5),
    surfaceVariant = Color(0xFF1B2B39),
    onSurfaceVariant = Color(0xFFB8CAD8),
    surfaceTint = Color(0xFF7CD0FF),
    outline = Color(0xFF8799A8),
    outlineVariant = Color(0xFF30434F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun Ts3MobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) IcyDark else IcyLight
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
