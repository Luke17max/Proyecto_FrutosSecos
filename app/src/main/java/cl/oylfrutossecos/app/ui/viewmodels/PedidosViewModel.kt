package cl.oylfrutossecos.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.domain.model.EstadoPedido
import cl.oylfrutossecos.app.domain.model.ItemCarrito
import cl.oylfrutossecos.app.domain.model.Pedido
import cl.oylfrutossecos.app.domain.model.TipoEntrega
import cl.oylfrutossecos.app.utils.NotificacionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================================================
// VIEWMODEL DE PEDIDOS Y TRANSACCIONES (MVVM + NOTIFICACIONES NATIVAS)
// ============================================================================

data class PedidosUiState(
    val pedidos: List<Pedido> = emptyList(),
    val filtroEstado: String = "TODOS", // "TODOS", "EN_PROCESO", "COMPLETADOS"
    val ultimoPedidoConfirmado: Pedido? = null,
    val procesando: Boolean = false,
    val errorTransaccion: String? = null
)

class PedidosViewModel(
    private val repository: OylRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    fun cargarPedidosCliente(usuarioId: Long) {
        viewModelScope.launch {
            val lista = repository.obtenerPedidosUsuario(usuarioId)
            _uiState.update { it.copy(pedidos = lista) }
        }
    }

    fun cargarTodosLosPedidosAdmin() {
        viewModelScope.launch {
            val lista = repository.obtenerTodosLosPedidos()
            _uiState.update { it.copy(pedidos = lista) }
        }
    }

    fun cambiarFiltro(filtro: String) {
        _uiState.update { it.copy(filtroEstado = filtro) }
    }

    fun confirmarPedido(
        usuarioId: Long,
        clienteNombre: String,
        items: List<ItemCarrito>,
        costoDespacho: Int,
        tipoEntrega: TipoEntrega,
        direccionEntrega: String,
        alExito: (Pedido) -> Unit,
        alError: (String) -> Unit
    ) {
        _uiState.update { it.copy(procesando = true, errorTransaccion = null) }

        viewModelScope.launch {
            val resultado = repository.procesarPedido(
                usuarioId = usuarioId,
                clienteNombre = clienteNombre,
                items = items,
                costoDespacho = costoDespacho,
                tipoEntrega = tipoEntrega,
                direccionEntrega = direccionEntrega
            )

            _uiState.update { it.copy(procesando = false) }

            resultado.onSuccess { pedido ->
                _uiState.update { it.copy(ultimoPedidoConfirmado = pedido) }

                // Disparar Notificación Nativa en el teléfono
                NotificacionHelper.enviarNotificacion(
                    context = context,
                    titulo = "¡Pedido Confirmado!",
                    mensaje = "Tu solicitud ${pedido.numeroPedido} por $${pedido.total} ha sido recibida con éxito."
                )

                alExito(pedido)
            }.onFailure { err ->
                val msg = err.message ?: "Error al procesar el pedido"
                _uiState.update { it.copy(errorTransaccion = msg) }
                alError(msg)
            }
        }
    }

    fun actualizarEstadoPedido(pedidoId: Long, nuevoEstado: EstadoPedido, esAdmin: Boolean, usuarioId: Long) {
        viewModelScope.launch {
            val exito = repository.actualizarEstadoPedido(pedidoId, nuevoEstado)
            if (exito) {
                // Notificación nativa si el estado avanzó
                NotificacionHelper.enviarNotificacion(
                    context = context,
                    titulo = "Actualización de Pedido",
                    mensaje = "El pedido ha cambiado a estado: ${nuevoEstado.etiqueta}"
                )

                if (esAdmin) {
                    cargarTodosLosPedidosAdmin()
                } else {
                    cargarPedidosCliente(usuarioId)
                }
            }
        }
    }
}
