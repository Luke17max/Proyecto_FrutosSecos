package cl.oylfrutossecos.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.domain.model.CampoFormulario
import cl.oylfrutossecos.app.domain.model.Categoria
import cl.oylfrutossecos.app.domain.model.Producto
import cl.oylfrutossecos.app.utils.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================================================
// VIEWMODEL DE CATÁLOGO Y GESTIÓN DE PRODUCTOS
// ============================================================================

data class FormularioProductoUiState(
    val id: Long = 0,
    val codigoQr: CampoFormulario = CampoFormulario(),
    val nombre: CampoFormulario = CampoFormulario(),
    val categoria: Categoria = Categoria.FRUTOS_SECOS,
    val precio: CampoFormulario = CampoFormulario(),
    val stock: CampoFormulario = CampoFormulario(),
    val formato: CampoFormulario = CampoFormulario(valor = "250 gr"),
    val descripcion: CampoFormulario = CampoFormulario(),
    val imagenUri: String? = null,
    val activo: Boolean = true,
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val mensajeError: String? = null
) {
    val esValido: Boolean
        get() = nombre.valor.isNotBlank() && precio.valor.isNotBlank() &&
                stock.valor.isNotBlank() && descripcion.valor.isNotBlank() &&
                nombre.esValido && precio.esValido && stock.esValido && descripcion.esValido
}

class ProductosViewModel(private val repository: OylRepository) : ViewModel() {

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow(Categoria.TODOS)
    val categoriaSeleccionada: StateFlow<Categoria> = _categoriaSeleccionada.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado.asStateFlow()

    private val _formularioProducto = MutableStateFlow(FormularioProductoUiState())
    val formularioProducto: StateFlow<FormularioProductoUiState> = _formularioProducto.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            val lista = repository.obtenerProductos(
                categoria = _categoriaSeleccionada.value,
                busqueda = _busqueda.value
            )
            _productos.value = lista
        }
    }

    fun seleccionarCategoria(cat: Categoria) {
        _categoriaSeleccionada.value = cat
        cargarProductos()
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
        cargarProductos()
    }

    fun seleccionarProducto(producto: Producto) {
        _productoSeleccionado.value = producto
    }

    fun seleccionarProductoPorId(id: Long) {
        viewModelScope.launch {
            val prod = repository.obtenerProductoPorId(id)
            _productoSeleccionado.value = prod
        }
    }

    fun seleccionarProductoPorQr(codigoQr: String, alEncontrar: (Producto) -> Unit, alFallar: (String) -> Unit) {
        viewModelScope.launch {
            val prod = repository.obtenerProductoPorQr(codigoQr)
            if (prod != null) {
                _productoSeleccionado.value = prod
                alEncontrar(prod)
            } else {
                alFallar("No se encontró ningún producto con el código: $codigoQr")
            }
        }
    }

    // ------------------------------------------------------------------------
    // GESTIÓN DE PRODUCTOS (ADMIN)
    // ------------------------------------------------------------------------

    fun iniciarCreacionProducto() {
        val nuevoCodigo = "OYL-PRD-${(100 + (Math.random() * 900).toInt())}"
        _formularioProducto.value = FormularioProductoUiState(
            codigoQr = CampoFormulario(valor = nuevoCodigo)
        )
    }

    fun iniciarEdicionProducto(producto: Producto) {
        _formularioProducto.value = FormularioProductoUiState(
            id = producto.id,
            codigoQr = CampoFormulario(valor = producto.codigoQr),
            nombre = CampoFormulario(valor = producto.nombre),
            categoria = producto.categoria,
            precio = CampoFormulario(valor = producto.precio.toString()),
            stock = CampoFormulario(valor = producto.stock.toString()),
            formato = CampoFormulario(valor = producto.formato),
            descripcion = CampoFormulario(valor = producto.descripcion),
            imagenUri = producto.imagenUri,
            activo = producto.activo
        )
    }

    fun cambiarNombreProducto(valor: String) = _formularioProducto.update {
        it.copy(nombre = CampoFormulario(valor, Validadores.validarTextoRequerido(valor, "Nombre")), mensajeError = null)
    }

    fun cambiarCategoriaProducto(cat: Categoria) = _formularioProducto.update {
        it.copy(categoria = cat)
    }

    fun cambiarPrecioProducto(valor: String) = _formularioProducto.update {
        it.copy(precio = CampoFormulario(valor, Validadores.validarPrecio(valor)), mensajeError = null)
    }

    fun cambiarStockProducto(valor: String) = _formularioProducto.update {
        it.copy(stock = CampoFormulario(valor, Validadores.validarStock(valor)), mensajeError = null)
    }

    fun cambiarFormatoProducto(valor: String) = _formularioProducto.update {
        it.copy(formato = CampoFormulario(valor, Validadores.validarTextoRequerido(valor, "Formato")), mensajeError = null)
    }

    fun cambiarDescripcionProducto(valor: String) = _formularioProducto.update {
        it.copy(descripcion = CampoFormulario(valor, Validadores.validarTextoRequerido(valor, "Descripción", 5)), mensajeError = null)
    }

    fun asignarImagenProducto(uri: String) = _formularioProducto.update {
        it.copy(imagenUri = uri)
    }

    fun cambiarActivoProducto(activo: Boolean) = _formularioProducto.update {
        it.copy(activo = activo)
    }

    fun guardarProducto(alCompletar: () -> Unit) {
        val actual = _formularioProducto.value
        val validado = actual.copy(
            nombre = actual.nombre.copy(error = Validadores.validarTextoRequerido(actual.nombre.valor, "Nombre")),
            precio = actual.precio.copy(error = Validadores.validarPrecio(actual.precio.valor)),
            stock = actual.stock.copy(error = Validadores.validarStock(actual.stock.valor)),
            descripcion = actual.descripcion.copy(error = Validadores.validarTextoRequerido(actual.descripcion.valor, "Descripción", 5))
        )
        _formularioProducto.value = validado

        if (!validado.esValido) {
            _formularioProducto.update { it.copy(mensajeError = "Por favor verifica los campos obligatorios") }
            return
        }

        _formularioProducto.update { it.copy(cargando = true) }

        viewModelScope.launch {
            val producto = Producto(
                id = validado.id,
                codigoQr = validado.codigoQr.valor.ifBlank { "OYL-AUTO-${System.currentTimeMillis() % 1000}" },
                nombre = validado.nombre.valor.trim(),
                categoria = validado.categoria,
                precio = validado.precio.valor.trim().toInt(),
                stock = validado.stock.valor.trim().toInt(),
                formato = validado.formato.valor.trim().ifBlank { "250 gr" },
                descripcion = validado.descripcion.valor.trim(),
                imagenUri = validado.imagenUri,
                activo = validado.activo
            )

            repository.guardarOActualizarProducto(producto)
            cargarProductos()
            _formularioProducto.update { it.copy(cargando = false, guardadoExitoso = true) }
            alCompletar()
        }
    }
}
