package cl.oylfrutossecos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.ui.OYLFrutosSecosApp
import cl.oylfrutossecos.app.ui.theme.OYLFrutosSecosTheme
import cl.oylfrutossecos.app.ui.viewmodels.*
import cl.oylfrutossecos.app.utils.NotificacionHelper

// ============================================================================
// ACTIVIDAD PRINCIPAL - OYL FRUTOS SECOS
// ============================================================================
// Punto de entrada de la aplicación en Android Studio.
// Configura modo Edge-To-Edge, inicializa canales de notificación nativos
// y suministra los ViewModels a la jerarquía de Jetpack Compose.
// ============================================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración nativa moderna Edge-to-Edge
        enableEdgeToEdge()

        // Inicializar canal del sistema para notificaciones locales
        NotificacionHelper.inicializarCanal(this)

        val repository = OylRepository.obtenerInstancia(this)

        setContent {
            OYLFrutosSecosTheme {
                val authViewModel = viewModel { AuthViewModel(repository) }
                val productosViewModel = viewModel { ProductosViewModel(repository) }
                val carritoViewModel = viewModel { CarritoViewModel(repository) }
                val pedidosViewModel = viewModel { PedidosViewModel(repository, applicationContext) }
                val inventarioViewModel = viewModel { InventarioViewModel(repository) }
                val notificacionesViewModel = viewModel { NotificacionesViewModel(repository) }

                OYLFrutosSecosApp(
                    authViewModel = authViewModel,
                    productosViewModel = productosViewModel,
                    carritoViewModel = carritoViewModel,
                    pedidosViewModel = pedidosViewModel,
                    inventarioViewModel = inventarioViewModel,
                    notificacionesViewModel = notificacionesViewModel
                )
            }
        }
    }
}
