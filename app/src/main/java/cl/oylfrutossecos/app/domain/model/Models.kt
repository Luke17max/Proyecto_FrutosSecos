package cl.oylfrutossecos.app.domain.model

/**
 * Roles de usuario admitidos por la plataforma.
 */
enum class Rol {
    CLIENTE,
    ADMINISTRADOR
}

/**
 * Categorías oficiales del catálogo de OYL Frutos Secos.
 */
enum class Categoria(val titulo: String) {
    TODOS("Todos"),
    FRUTOS_SECOS("Frutos secos"),
    SEMILLAS("Semillas"),
    DESHIDRATADOS("Deshidratados")
}

/**
 * Entidad de Usuario del sistema (persiste en SQLite).
 */
data class Usuario(
    val id: Long = 0,
    val nombre: String,
    val alias: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val contrasena: String,
    val rol: Rol = Rol.CLIENTE,
    val avatarUri: String? = null
)

/**
 * Entidad de Producto comercializado (persiste en SQLite y sincroniza con backend).
 */
data class Producto(
    val id: Long = 0,
    val codigoQr: String, // Identificador para el escáner (ej: "OYL-ALM-001")
    val nombre: String,
    val categoria: Categoria,
    val precio: Int,
    val stock: Int,
    val formato: String, // Ej: "250 gr", "500 gr", "1 Kg"
    val descripcion: String,
    val imagenUri: String? = null,
    val activo: Boolean = true
) {
    val tieneStock: Boolean get() = stock > 0
    val stockBajo: Boolean get() = stock in 1..10
}

/**
 * Elemento individual del carrito de compras, aislado por cliente.
 */
data class ItemCarrito(
    val id: Long = 0,
    val usuarioId: Long,
    val producto: Producto,
    val cantidad: Int
) {
    val subtotal: Int get() = producto.precio * cantidad
}

/**
 * Estados del ciclo de vida de un pedido.
 */
enum class EstadoPedido(val etiqueta: String) {
    PENDIENTE_VALIDACION("Pendiente de Validación"),
    CONFIRMADO("Confirmado"),
    PREPARADO("Preparado"),
    ENTREGADO("Entregado")
}

/**
 * Modalidad de entrega del pedido.
 */
enum class TipoEntrega(val etiqueta: String) {
    RETIRO_TIENDA("Retiro en tienda"),
    DESPACHO_DOMICILIO("Despacho a domicilio")
}

/**
 * Pedido transaccional consolidado.
 */
data class Pedido(
    val id: Long = 0,
    val numeroPedido: String, // Ej: "#001245"
    val usuarioId: Long,
    val clienteNombre: String,
    val items: List<ItemCarrito>,
    val total: Int,
    val costoDespacho: Int = 0,
    val tipoEntrega: TipoEntrega = TipoEntrega.RETIRO_TIENDA,
    val direccionEntrega: String = "",
    val estado: EstadoPedido = EstadoPedido.PENDIENTE_VALIDACION,
    val fecha: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Registro de notificaciones centralizadas.
 */
data class Notificacion(
    val id: Long = 0,
    val usuarioId: Long?, // null indica aviso global/admin
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    val leida: Boolean = false,
    val destinoRuta: String? = null
)

/**
 * Punto geográfico para la tienda o punto de retiro (osmdroid).
 */
data class PuntoRetiro(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val horario: String,
    val telefono: String,
    val latitud: Double,
    val longitud: Double,
    val esTiendaPrincipal: Boolean = false
)

/**
 * Campo individual de formulario con texto y mensaje de fallo desacoplado.
 */
data class CampoFormulario(
    val valor: String = "",
    val error: String? = null
) {
    val esValido: Boolean get() = error == null
}