package cl.oylfrutossecos.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.domain.model.Notificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================================================
// VIEWMODEL DE CENTRO DE NOTIFICACIONES
// ============================================================================

data class NotificacionesUiState(
    val notificaciones: List<Notificacion> = emptyList(),
    val filtro: String = "TODAS" // "TODAS", "PEDIDOS", "PROMOCIONES"
)

class NotificacionesViewModel(private val repository: OylRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    fun cargarNotificaciones(usuarioId: Long, esAdmin: Boolean) {
        viewModelScope.launch {
            val lista = repository.obtenerNotificaciones(usuarioId, esAdmin)
            _uiState.update { it.copy(notificaciones = lista) }
        }
    }

    fun marcarComoLeida(id: Long, usuarioId: Long, esAdmin: Boolean) {
        viewModelScope.launch {
            repository.marcarNotificacionLeida(id)
            cargarNotificaciones(usuarioId, esAdmin)
        }
    }

    fun cambiarFiltro(filtro: String) {
        _uiState.update { it.copy(filtro = filtro) }
    }
}
