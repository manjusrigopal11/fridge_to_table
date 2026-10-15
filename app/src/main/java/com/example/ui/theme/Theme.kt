package com.example.ui.theme

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
  primary = EmeraldDarkPrimary,
  onPrimary = Color(0xFF003828),
  primaryContainer = SageDarkContainer,
  onPrimaryContainer = MintContainer,
  secondary = TerracottaDarkAccent,
  onSecondary = Color(0xFF5E1708),
  secondaryContainer = Color(0xFF43180F),
  onSecondaryContainer = WarmTangerineContainer,
  tertiary = GoldenHoney,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldPrimary,
  onPrimary = Color.White,
  primaryContainer = MintContainer,
  onPrimaryContainer = OnMintContainer,
  secondary = TerracottaAccent,
  onSecondary = Color.White,
  secondaryContainer = WarmTangerineContainer,
  onSecondaryContainer = OnTangerineContainer,
  tertiary = GoldenHoney,
  background = BackgroundLinen,
  onBackground = TextPrimary,
  surface = SurfaceCream,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceVariantWarm,
  onSurfaceVariant = TextSecondary,
  outline = OutlineWarm
)

@Composable
fun FridgeToTableTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our handcrafted culinary palette for distinct brand identity
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
