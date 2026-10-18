package cl.oylfrutossecos.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class Pantalla(val ruta: String, val titulo: String) {
    BIENVENIDA("bienvenida", "Bienvenido a OYL"),
    REGISTRO("registro", "Crear Cuenta"),
    LOGIN("login", "Iniciar Sesión"),
    DASHBOARD("dashboard", "OYL Frutos Secos"),
    MAPA_TIENDAS("mapa", "Nuestras Tiendas y Retiro"),
    CATALOGO("catalogo", "Catálogo de Productos"),
    DETALLE_PRODUCTO("detalle_producto", "Detalle del Producto"),
    ESCANER_QR("escaner_qr", "Escáner de Productos"),
    CARRITO("carrito", "Mi Carrito"),
    CONFIRMACION_PEDIDO("confirmacion", "Pedido Confirmado"),
    GESTION_PRODUCTOS("gestion_productos", "Gestión de Productos"),
    GESTION_INVENTARIO("gestion_inventario", "Control de Inventario"),
    HISTORIAL_PEDIDOS("historial_pedidos", "Mis Pedidos"),
    NOTIFICACIONES("notificaciones", "Notificaciones y Avisos"),
    PERFIL("perfil", "Mi Perfil")
}

/**
 * Elementos de la barra inferior de navegación (BottomNavigation).
 */
data class DestinoBarraInferior(
    val pantalla: Pantalla,
    val etiqueta: String,
    val icono: ImageVector
)

val DestinosBarraCliente = listOf(
    DestinoBarraInferior(Pantalla.DASHBOARD, "Inicio", Icons.Default.Home),
    DestinoBarraInferior(Pantalla.CATALOGO, "Catálogo", Icons.Default.Storefront),
    DestinoBarraInferior(Pantalla.MAPA_TIENDAS, "Mapa", Icons.Default.Map),
    DestinoBarraInferior(Pantalla.ESCANER_QR, "Escanear", Icons.Default.QrCodeScanner),
    DestinoBarraInferior(Pantalla.HISTORIAL_PEDIDOS, "Pedidos", Icons.Default.ReceiptLong),
    DestinoBarraInferior(Pantalla.PERFIL, "Perfil", Icons.Default.Person)
)

val DestinosBarraAdmin = listOf(
    DestinoBarraInferior(Pantalla.DASHBOARD, "Inicio", Icons.Default.Dashboard),
    DestinoBarraInferior(Pantalla.GESTION_PRODUCTOS, "Productos", Icons.Default.Inventory),
    DestinoBarraInferior(Pantalla.GESTION_INVENTARIO, "Inventario", Icons.Default.FactCheck),
    DestinoBarraInferior(Pantalla.HISTORIAL_PEDIDOS, "Pedidos", Icons.Default.ReceiptLong),
    DestinoBarraInferior(Pantalla.NOTIFICACIONES, "Avisos", Icons.Default.Notifications),
    DestinoBarraInferior(Pantalla.PERFIL, "Perfil", Icons.Default.Person)
)