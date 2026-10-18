package cl.oylfrutossecos.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.domain.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventarioUiState(
    val productos: List<Producto> = emptyList(),
    val busqueda: String = "",
    val productoSeleccionado: Producto? = null,
    val cantidadAjuste: Int = 1,
    val soloStockBajo: Boolean = false,
    val mensajeConfirmacion: String? = null
)

class InventarioViewModel(private val repository: OylRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    init {
        cargarInventario()
    }

    fun cargarInventario() {
        viewModelScope.launch {
            val lista = repository.obtenerProductos(busqueda = _uiState.value.busqueda)
            _uiState.update { it.copy(productos = lista) }
        }
    }

    fun cambiarBusqueda(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        cargarInventario()
    }

    fun alternarFiltroStockBajo() {
        _uiState.update { it.copy(soloStockBajo = !it.soloStockBajo) }
    }

    fun seleccionarProducto(producto: Producto?) {
        _uiState.update { it.copy(productoSeleccionado = producto, cantidadAjuste = 1) }
    }

    fun seleccionarPorQr(codigoQr: String, alEncontrar: (Producto) -> Unit, alFallar: (String) -> Unit) {
        viewModelScope.launch {
            val prod = repository.obtenerProductoPorQr(codigoQr)
            if (prod != null) {
                seleccionarProducto(prod)
                alEncontrar(prod)
            } else {
                alFallar("Código QR desconocido: $codigoQr")
            }
        }
    }

    fun registrarMovimiento(esEntrada: Boolean) {
        val prod = _uiState.value.productoSeleccionado ?: return
        val ajuste = _uiState.value.cantidadAjuste
        val nuevoStock = if (esEntrada) prod.stock + ajuste else maxOf(0, prod.stock - ajuste)

        viewModelScope.launch {
            val exito = repository.actualizarStock(prod.id, nuevoStock)
            if (exito) {
                val tipo = if (esEntrada) "Entrada (+ $ajuste)" else "Salida (- $ajuste)"
                _uiState.update {
                    it.copy(
                        productoSeleccionado = prod.copy(stock = nuevoStock),
                        mensajeConfirmacion = "$tipo registrada con éxito para ${prod.nombre}. Stock actual: $nuevoStock un."
                    )
                }
                cargarInventario()
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeConfirmacion = null) }
    }
}