package cl.oylfrutossecos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.domain.model.Producto
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.domain.model.Usuario
import cl.oylfrutossecos.app.ui.components.AlertaStockBajoBadge
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.CarritoViewModel
import cl.oylfrutossecos.app.ui.viewmodels.PedidosViewModel
import cl.oylfrutossecos.app.ui.viewmodels.ProductosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usuario: Usuario,
    productosViewModel: ProductosViewModel,
    carritoViewModel: CarritoViewModel,
    pedidosViewModel: PedidosViewModel,
    alNavegarACatalogo: () -> Unit,
    alNavegarAMapa: () -> Unit,
    alNavegarAQr: () -> Unit,
    alNavegarAPedidos: () -> Unit,
    alNavegarAGestionProductos: () -> Unit,
    alNavegarAGestionInventario: () -> Unit,
    alSeleccionarProducto: (Producto) -> Unit
) {
    val productos by productosViewModel.productos.collectAsStateWithLifecycle()
    val pedidosState by pedidosViewModel.uiState.collectAsStateWithLifecycle()

    val productosStockBajo = remember(productos) {
        productos.filter { it.stockBajo }
    }
    val pedidosPendientes = remember(pedidosState.pedidos) {
        pedidosState.pedidos.filter { it.estado == cl.oylfrutossecos.app.domain.model.EstadoPedido.PENDIENTE_VALIDACION }
    }

    LaunchedEffect(usuario.id) {
        if (usuario.rol == Rol.ADMINISTRADOR) {
            pedidosViewModel.cargarTodosLosPedidosAdmin()
        } else {
            pedidosViewModel.cargarPedidosCliente(usuario.id)
            carritoViewModel.establecerUsuario(usuario.id, usuario.direccion)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "¡Hola, ${usuario.nombre}!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (usuario.rol == Rol.ADMINISTRADOR) "Panel de Administración OYL" else "Qué bueno verte de nuevo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VerdeOlivaClaro),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (usuario.rol == Rol.ADMINISTRADOR) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = VerdeOlivaPrincipal
                        )
                    }
                }
            )
        }
    ) { margen ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(margen)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (usuario.rol == Rol.ADMINISTRADOR) {
                // VISTA DASHBOARD ADMINISTRADOR

                // Banner de Alerta si hay stock bajo
                if (productosStockBajo.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RojoAlertaContenedor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = alNavegarAGestionInventario)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RojoAlerta, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¡Alerta de Inventario!",
                                    fontWeight = FontWeight.Bold,
                                    color = RojoAlerta,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${productosStockBajo.size} producto(s) con existencias críticas (menos de 10 unidades). Toca para ajustar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RojoAlerta
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RojoAlerta)
                        }
                    }
                }

                // Tarjetas de Métricas Rápidas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaMetrica(
                        titulo = "Pendientes",
                        valor = pedidosPendientes.size.toString(),
                        icono = Icons.Default.HourglassBottom,
                        color = NaranjaPendiente,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaMetrica(
                        titulo = "Stock Bajo",
                        valor = productosStockBajo.size.toString(),
                        icono = Icons.Default.ProductionQuantityLimits,
                        color = RojoAlerta,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaMetrica(
                        titulo = "Catálogo",
                        valor = productos.size.toString(),
                        icono = Icons.Default.Inventory2,
                        color = VerdeOlivaPrincipal,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Acciones Operativas Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BotonAccionDashboard(
                        titulo = "Productos",
                        subtitulo = "Crear y editar",
                        icono = Icons.Default.AddBox,
                        color = VerdeOlivaPrincipal,
                        alHacerClic = alNavegarAGestionProductos,
                        modifier = Modifier.weight(1f)
                    )
                    BotonAccionDashboard(
                        titulo = "Inventario",
                        subtitulo = "Entradas / Salidas",
                        icono = Icons.Default.SwapVert,
                        color = AlmendraDorado,
                        alHacerClic = alNavegarAGestionInventario,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BotonAccionDashboard(
                        titulo = "Pedidos",
                        subtitulo = "Cambiar estados",
                        icono = Icons.Default.FactCheck,
                        color = AzulProceso,
                        alHacerClic = alNavegarAPedidos,
                        modifier = Modifier.weight(1f)
                    )
                    BotonAccionDashboard(
                        titulo = "Escáner QR",
                        subtitulo = "Reconocer ítems",
                        icono = Icons.Default.QrCodeScanner,
                        color = MarronNuez,
                        alHacerClic = alNavegarAQr,
                        modifier = Modifier.weight(1f)
                    )
                }

            } else {
                // VISTA DASHBOARD CLIENTE

                // Banner Promocional Destacado
                Card(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(VerdeOlivaPrincipal, AlmendraDorado)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Surface(
                                color = SuperficieTarjeta.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "PROMOCIÓN DESTACADA",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Compra saludable, vida mejor",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aprovecha 20% de descuento en Mix Premium de 250gr",
                                style = MaterialTheme.typography.bodySmall,
                                color = CremaFondo
                            )
                        }
                    }
                }

                // Accesos Rápidos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BotonAccionCircular(Icons.Default.Storefront, "Catálogo", alNavegarACatalogo)
                    BotonAccionCircular(Icons.Default.LocationOn, "Tiendas", alNavegarAMapa)
                    BotonAccionCircular(Icons.Default.QrCodeScanner, "Escanear", alNavegarAQr)
                    BotonAccionCircular(Icons.Default.ReceiptLong, "Mis pedidos", alNavegarAPedidos)
                }

                // Productos Destacados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Productos Destacados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = alNavegarACatalogo) {
                        Text("Ver todos")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productos.take(5)) { producto ->
                        TarjetaProductoHorizontal(
                            producto = producto,
                            alSeleccionar = { alSeleccionarProducto(producto) },
                            alAgregar = {
                                carritoViewModel.agregarAlCarrito(producto.id, 1) { _, _ -> }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaMetrica(
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BotonAccionDashboard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    color: Color,
    alHacerClic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = alHacerClic,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BotonAccionCircular(
    icono: ImageVector,
    etiqueta: String,
    alHacerClic: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = alHacerClic)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(VerdeOlivaClaro),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icono, contentDescription = etiqueta, tint = VerdeOlivaPrincipal, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = etiqueta, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TarjetaProductoHorizontal(
    producto: Producto,
    alSeleccionar: () -> Unit,
    alAgregar: () -> Unit
) {
    Card(
        onClick = alSeleccionar,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SuperficieVariante),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = VerdeOlivaPrincipal,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "$${producto.precio}",
                style = MaterialTheme.typography.titleMedium,
                color = VerdeOlivaPrincipal,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Stock: ${producto.stock} un.",
                style = MaterialTheme.typography.labelSmall,
                color = if (producto.stockBajo) RojoAlerta else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = alAgregar,
                enabled = producto.tieneStock,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (producto.tieneStock) "Añadir" else "Agotado", fontSize = 12.sp)
            }
        }
    }
}