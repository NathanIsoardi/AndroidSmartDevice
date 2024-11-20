package fr.isen.Isoardi.androidsmartdevice.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LightGray,
    secondary = MediumGray,
    tertiary = DarkGray
)

private val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    secondary = MediumBlue,
    tertiary = DarkBlue

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AndroidSmartDeviceTheme(content: @Composable () -> Unit) {
    val customColorScheme = lightColorScheme(
        primary = Color(0xFF2A2727),
        onPrimary = Color.White,
        secondary = Color(0xFF03DAC6),
        onSecondary = Color.Black,
        error = Color(0xFFB00020),
        onError = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
    )

    MaterialTheme(
        colorScheme = customColorScheme,
        typography = Typography,
        content = content
    )
}