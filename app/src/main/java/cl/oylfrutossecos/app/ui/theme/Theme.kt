package cl.oylfrutossecos.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EsquemaLuz = lightColorScheme(
    primary = VerdeOlivaPrincipal,
    onPrimary = SuperficieTarjeta,
    primaryContainer = VerdeOlivaClaro,
    onPrimaryContainer = VerdeOlivaOscuro,
    secondary = AlmendraDorado,
    onSecondary = TextoPrincipal,
    background = BlancoFondo,
    onBackground = TextoPrincipal,
    surface = SuperficieTarjeta,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,
    error = RojoAlerta,
    errorContainer = RojoAlertaContenedor
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeOlivaClaro,
    onPrimary = VerdeOlivaOscuro,
    primaryContainer = VerdeOlivaPrincipal,
    secondary = AlmendraDorado,
    background = TextoPrincipal,
    surface = Color(0xFF2C2B2E),
    surfaceVariant = Color(0xFF3B393E)
)

@Composable
fun OYLFrutosSecosTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colores = if (oscuro) EsquemaOscuro else EsquemaLuz
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colores.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colores,
        typography = Typography,
        content = content
    )
}