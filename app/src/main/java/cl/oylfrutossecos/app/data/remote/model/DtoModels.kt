package cl.oylfrutossecos.app.data.remote.model

import com.google.gson.annotations.SerializedName

data class ProductoDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("codigoQr") val codigoQr: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("categoria") val categoria: String = "TODOS",
    @SerializedName("precio") val precio: Int = 0,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("formato") val formato: String = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("imagenUri") val imagenUri: String? = null,
    @SerializedName("activo") val activo: Boolean = true
)

data class AjusteStockDto(
    @SerializedName("delta") val delta: Int
)

data class UsuarioDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("alias") val alias: String = "",
    @SerializedName("correo") val correo: String = "",
    @SerializedName("telefono") val telefono: String = "",
    @SerializedName("direccion") val direccion: String = "",
    @SerializedName("contrasena") val contrasena: String = "",
    @SerializedName("rol") val rol: String = "CLIENTE",
    @SerializedName("avatarUri") val avatarUri: String? = null
)

data class DetallePedidoDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("productoId") val productoId: Long = 0,
    @SerializedName("nombreProducto") val nombreProducto: String = "",
    @SerializedName("precioUnitario") val precioUnitario: Int = 0,
    @SerializedName("cantidad") val cantidad: Int = 0,
    @SerializedName("subtotal") val subtotal: Int = 0
)

data class PedidoDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("numeroPedido") val numeroPedido: String? = null,
    @SerializedName("clienteId") val clienteId: Long = 0,
    @SerializedName("clienteNombre") val clienteNombre: String = "",
    @SerializedName("fechaHora") val fechaHora: String? = null,
    @SerializedName("tipoEntrega") val tipoEntrega: String = "RETIRO_TIENDA",
    @SerializedName("direccionEntrega") val direccionEntrega: String = "",
    @SerializedName("subtotal") val subtotal: Int = 0,
    @SerializedName("costoEnvio") val costoEnvio: Int = 0,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("estado") val estado: String = "PENDIENTE_VALIDACION",
    @SerializedName("items") val items: List<DetallePedidoDto> = emptyList()
)

data class EstadoPedidoDto(
    @SerializedName("estado") val estado: String
)

data class PostExternoDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
