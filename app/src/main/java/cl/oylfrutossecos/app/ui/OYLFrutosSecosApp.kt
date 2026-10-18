package cl.oylfrutossecos.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.domain.model.Producto
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.domain.model.Usuario
import cl.oylfrutossecos.app.navigation.*
import cl.oylfrutossecos.app.ui.screens.*
import cl.oylfrutossecos.app.ui.viewmodels.*

@Composable
fun OYLFrutosSecosApp(
    authViewModel: AuthViewModel,
    productosViewModel: ProductosViewModel,
    carritoViewModel: CarritoViewModel,
    pedidosViewModel: PedidosViewModel,
    inventarioViewModel: InventarioViewModel,
    notificacionesViewModel: NotificacionesViewModel
) {
    val usuarioActivo by authViewModel.usuarioActivo.collectAsStateWithLifecycle()
    val productoSeleccionado by productosViewModel.productoSeleccionado.collectAsStateWithLifecycle()
    val ultimoPedidoConfirmado by pedidosViewModel.uiState.collectAsStateWithLifecycle()

    var pantallaActual by rememberSaveable { mutableStateOf(Pantalla.BIENVENIDA) }

    // Si el usuario ya tiene sesión iniciada y está en bienvenida/login, redirigir al Dashboard
    LaunchedEffect(usuarioActivo) {
        if (usuarioActivo != null && (pantallaActual == Pantalla.BIENVENIDA || pantallaActual == Pantalla.LOGIN || pantallaActual == Pantalla.REGISTRO)) {
            pantallaActual = Pantalla.DASHBOARD
        }
    }

    val mostrarBarraInferior = usuarioActivo != null && pantallaActual in listOf(
        Pantalla.DASHBOARD,
        Pantalla.CATALOGO,
        Pantalla.MAPA_TIENDAS,
        Pantalla.ESCANER_QR,
        Pantalla.HISTORIAL_PEDIDOS,
        Pantalla.NOTIFICACIONES,
        Pantalla.PERFIL,
        Pantalla.GESTION_PRODUCTOS,
        Pantalla.GESTION_INVENTARIO
    )

    val destinosBarra = if (usuarioActivo?.rol == Rol.ADMINISTRADOR) DestinosBarraAdmin else DestinosBarraCliente

    Scaffold(
        bottomBar = {
            if (mostrarBarraInferior) {
                NavigationBar {
                    destinosBarra.forEach { destino ->
                        val seleccionada = pantallaActual == destino.pantalla
                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = { pantallaActual = destino.pantalla },
                            icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                            label = { Text(destino.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { margen ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(margen)
        ) {
            when (pantallaActual) {
                // 1. Bienvenida
                Pantalla.BIENVENIDA -> BienvenidaScreen(
                    alComenzar = { pantallaActual = Pantalla.LOGIN },
                    alVerCatalogo = { pantallaActual = Pantalla.CATALOGO },
                    alVerMapa = { pantallaActual = Pantalla.MAPA_TIENDAS }
                )

                // 2. Registro
                Pantalla.REGISTRO -> RegistroScreen(
                    viewModel = authViewModel,
                    alRegistrarExitoso = { pantallaActual = Pantalla.DASHBOARD },
                    alIrALogin = { pantallaActual = Pantalla.LOGIN },
                    alVolver = { pantallaActual = Pantalla.BIENVENIDA }
                )

                // 3. Login
                Pantalla.LOGIN -> LoginScreen(
                    viewModel = authViewModel,
                    alLoginExitoso = { pantallaActual = Pantalla.DASHBOARD },
                    alIrARegistro = { pantallaActual = Pantalla.REGISTRO },
                    alVolver = { pantallaActual = Pantalla.BIENVENIDA }
                )

                // 4. Dashboard
                Pantalla.DASHBOARD -> usuarioActivo?.let { usr ->
                    DashboardScreen(
                        usuario = usr,
                        productosViewModel = productosViewModel,
                        carritoViewModel = carritoViewModel,
                        pedidosViewModel = pedidosViewModel,
                        alNavegarACatalogo = { pantallaActual = Pantalla.CATALOGO },
                        alNavegarAMapa = { pantallaActual = Pantalla.MAPA_TIENDAS },
                        alNavegarAQr = { pantallaActual = Pantalla.ESCANER_QR },
                        alNavegarAPedidos = { pantallaActual = Pantalla.HISTORIAL_PEDIDOS },
                        alNavegarAGestionProductos = { pantallaActual = Pantalla.GESTION_PRODUCTOS },
                        alNavegarAGestionInventario = { pantallaActual = Pantalla.GESTION_INVENTARIO },
                        alSeleccionarProducto = { prod ->
                            productosViewModel.seleccionarProducto(prod)
                            pantallaActual = Pantalla.DETALLE_PRODUCTO
                        }
                    )
                } ?: run { pantallaActual = Pantalla.LOGIN }

                // 5. Mapa
                Pantalla.MAPA_TIENDAS -> MapaTiendasScreen(
                    repository = cl.oylfrutossecos.app.data.repository.OylRepository.obtenerInstancia(androidx.compose.ui.platform.LocalContext.current),
                    alVolver = { pantallaActual = if (usuarioActivo != null) Pantalla.DASHBOARD else Pantalla.BIENVENIDA }
                )

                // 6. Catálogo
                Pantalla.CATALOGO -> CatalogoScreen(
                    rolUsuario = usuarioActivo?.rol ?: Rol.CLIENTE,
                    productosViewModel = productosViewModel,
                    carritoViewModel = carritoViewModel,
                    alSeleccionarProducto = { prod ->
                        productosViewModel.seleccionarProducto(prod)
                        pantallaActual = Pantalla.DETALLE_PRODUCTO
                    },
                    alEditarProducto = { prod ->
                        productosViewModel.iniciarEdicionProducto(prod)
                        pantallaActual = Pantalla.GESTION_PRODUCTOS
                    },
                    alIrACarrito = { pantallaActual = Pantalla.CARRITO },
                    alVolver = { pantallaActual = if (usuarioActivo != null) Pantalla.DASHBOARD else Pantalla.BIENVENIDA }
                )

                // 7. Detalle de Producto
                Pantalla.DETALLE_PRODUCTO -> productoSeleccionado?.let { prod ->
                    DetalleProductoScreen(
                        producto = prod,
                        rolUsuario = usuarioActivo?.rol ?: Rol.CLIENTE,
                        carritoViewModel = carritoViewModel,
                        alEditar = {
                            productosViewModel.iniciarEdicionProducto(prod)
                            pantallaActual = Pantalla.GESTION_PRODUCTOS
                        },
                        alVolver = { pantallaActual = Pantalla.CATALOGO }
                    )
                } ?: run { pantallaActual = Pantalla.CATALOGO }

                // 8. Escáner QR
                Pantalla.ESCANER_QR -> EscanearQrScreen(
                    rolUsuario = usuarioActivo?.rol ?: Rol.CLIENTE,
                    productosViewModel = productosViewModel,
                    inventarioViewModel = inventarioViewModel,
                    alVerFicha = { prod ->
                        productosViewModel.seleccionarProducto(prod)
                        pantallaActual = Pantalla.DETALLE_PRODUCTO
                    },
                    alAjustarInventario = { prod ->
                        pantallaActual = Pantalla.GESTION_INVENTARIO
                    },
                    alVolver = { pantallaActual = Pantalla.DASHBOARD }
                )

                // 9. Carrito
                Pantalla.CARRITO -> usuarioActivo?.let { usr ->
                    CarritoScreen(
                        usuario = usr,
                        carritoViewModel = carritoViewModel,
                        pedidosViewModel = pedidosViewModel,
                        alPedidoConfirmado = {
                            pantallaActual = Pantalla.CONFIRMACION_PEDIDO
                        },
                        alSeguirComprando = { pantallaActual = Pantalla.CATALOGO },
                        alVolver = { pantallaActual = Pantalla.DASHBOARD }
                    )
                } ?: run { pantallaActual = Pantalla.LOGIN }

                // 10. Confirmación de Pedido
                Pantalla.CONFIRMACION_PEDIDO -> ultimoPedidoConfirmado.ultimoPedidoConfirmado?.let { ped ->
                    ConfirmacionPedidoScreen(
                        pedido = ped,
                        alVerMisPedidos = { pantallaActual = Pantalla.HISTORIAL_PEDIDOS },
                        alSeguirComprando = { pantallaActual = Pantalla.CATALOGO }
                    )
                } ?: run { pantallaActual = Pantalla.HISTORIAL_PEDIDOS }

                // 11. Gestión de Productos (Admin)
                Pantalla.GESTION_PRODUCTOS -> GestionProductosScreen(
                    productosViewModel = productosViewModel,
                    alVolver = { pantallaActual = Pantalla.DASHBOARD }
                )

                // 12. Gestión de Inventario (Admin)
                Pantalla.GESTION_INVENTARIO -> GestionInventarioScreen(
                    inventarioViewModel = inventarioViewModel,
                    alNavegarAQr = { pantallaActual = Pantalla.ESCANER_QR },
                    alVolver = { pantallaActual = Pantalla.DASHBOARD }
                )

                // 13. Historial de Pedidos
                Pantalla.HISTORIAL_PEDIDOS -> usuarioActivo?.let { usr ->
                    HistorialPedidosScreen(
                        usuario = usr,
                        pedidosViewModel = pedidosViewModel,
                        alVolver = { pantallaActual = Pantalla.DASHBOARD }
                    )
                } ?: run { pantallaActual = Pantalla.LOGIN }

                // 14. Notificaciones
                Pantalla.NOTIFICACIONES -> usuarioActivo?.let { usr ->
                    NotificacionesScreen(
                        usuario = usr,
                        notificacionesViewModel = notificacionesViewModel,
                        alNavegarRuta = { ruta ->
                            when (ruta) {
                                "pedidos" -> pantallaActual = Pantalla.HISTORIAL_PEDIDOS
                                "inventario" -> pantallaActual = Pantalla.GESTION_INVENTARIO
                                "catalogo" -> pantallaActual = Pantalla.CATALOGO
                                else -> pantallaActual = Pantalla.DASHBOARD
                            }
                        },
                        alVolver = { pantallaActual = Pantalla.DASHBOARD }
                    )
                } ?: run { pantallaActual = Pantalla.LOGIN }

                // 15. Perfil
                Pantalla.PERFIL -> usuarioActivo?.let { usr ->
                    PerfilScreen(
                        usuario = usr,
                        authViewModel = authViewModel,
                        alCerrarSesion = { pantallaActual = Pantalla.LOGIN },
                        alVolver = { pantallaActual = Pantalla.DASHBOARD }
                    )
                } ?: run { pantallaActual = Pantalla.LOGIN }
            }
        }
    }
}