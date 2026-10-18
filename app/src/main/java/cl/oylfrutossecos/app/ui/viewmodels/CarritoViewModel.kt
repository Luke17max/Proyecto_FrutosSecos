package cl.oylfrutossecos.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.domain.model.ItemCarrito
import cl.oylfrutossecos.app.domain.model.TipoEntrega
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarritoUiState(
    val items: List<ItemCarrito> = emptyList(),
    val subtotal: Int = 0,
    val costoDespacho: Int = 0,
    val total: Int = 0,
    val tipoEntrega: TipoEntrega = TipoEntrega.RETIRO_TIENDA,
    val direccionDespacho: String = "",
    val cargando: Boolean = false,
    val mensajeError: String? = null
) {
    val estaVacio: Boolean get() = items.isEmpty()
    val cantidadTotalArticulos: Int get() = items.sumOf { it.cantidad }
}

class CarritoViewModel(private val repository: OylRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CarritoUiState())
    val uiState: StateFlow<CarritoUiState> = _uiState.asStateFlow()

    private var usuarioActualId: Long = 0

    fun establecerUsuario(usuarioId: Long, direccionDefecto: String = "") {
        usuarioActualId = usuarioId
        _uiState.update { it.copy(direccionDespacho = direccionDefecto) }
        cargarCarrito()
    }

    fun cargarCarrito() {
        if (usuarioActualId == 0L) return
        viewModelScope.launch {
            val lista = repository.obtenerCarrito(usuarioActualId)
            calcularTotales(lista, _uiState.value.tipoEntrega)
        }
    }

    fun agregarAlCarrito(productoId: Long, cantidad: Int = 1, alCompletar: (Boolean, String?) -> Unit) {
        if (usuarioActualId == 0L) {
            alCompletar(false, "Debes iniciar sesión para agregar productos")
            return
        }

        viewModelScope.launch {
            val exito = repository.agregarAlCarrito(usuarioActualId, productoId, cantidad)
            if (exito) {
                cargarCarrito()
                alCompletar(true, "¡Producto añadido al carrito!")
            } else {
                alCompletar(false, "No hay suficiente stock disponible para este producto")
            }
        }
    }

    fun modificarCantidad(itemId: Long, nuevaCantidad: Int) {
        viewModelScope.launch {
            repository.modificarCantidadCarrito(itemId, nuevaCantidad)
            cargarCarrito()
        }
    }

    fun eliminarItem(itemId: Long) {
        viewModelScope.launch {
            repository.eliminarItemCarrito(itemId)
            cargarCarrito()
        }
    }

    fun cambiarTipoEntrega(tipo: TipoEntrega) {
        val costo = if (tipo == TipoEntrega.DESPACHO_DOMICILIO) 2000 else 0
        _uiState.update { it.copy(tipoEntrega = tipo, costoDespacho = costo) }
        calcularTotales(_uiState.value.items, tipo)
    }

    fun cambiarDireccionDespacho(direccion: String) {
        _uiState.update { it.copy(direccionDespacho = direccion) }
    }

    private fun calcularTotales(items: List<ItemCarrito>, tipoEntrega: TipoEntrega) {
        val sub = items.sumOf { it.subtotal }
        val despacho = if (tipoEntrega == TipoEntrega.DESPACHO_DOMICILIO) 2000 else 0
        val tot = sub + despacho
        _uiState.update {
            it.copy(
                items = items,
                subtotal = sub,
                costoDespacho = despacho,
                total = tot
            )
        }
    }
}