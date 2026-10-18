package cl.oylfrutossecos.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.domain.model.ItemCarrito
import cl.oylfrutossecos.app.domain.model.Pedido
import cl.oylfrutossecos.app.domain.model.TipoEntrega
import cl.oylfrutossecos.app.domain.model.Usuario
import cl.oylfrutossecos.app.ui.components.BotonConCarga
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.CarritoViewModel
import cl.oylfrutossecos.app.ui.viewmodels.PedidosViewModel

// ============================================================================
// PANTALLA 9: CARRITO DE COMPRAS
// ============================================================================
// Rol: Cliente
// Muestra productos seleccionados, subtotales, cantidades y modalidades
// de despacho o retiro. Conserva el carrito en SQLite aislado por cliente.
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    usuario: Usuario,
    carritoViewModel: CarritoViewModel,
    pedidosViewModel: PedidosViewModel,
    alPedidoConfirmado: (Pedido) -> Unit,
    alSeguirComprando: () -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val estadoCarrito by carritoViewModel.uiState.collectAsStateWithLifecycle()
    val estadoPedidos by pedidosViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(usuario.id) {
        carritoViewModel.establecerUsuario(usuario.id, usuario.direccion)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito (${estadoCarrito.cantidadTotalArticulos})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { margen ->
        if (estadoCarrito.estaVacio) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(margen)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SuperficieVariante),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = VerdeOlivaPrincipal,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Tu carrito está vacío", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explora nuestro catálogo y agrega tus frutos secos y semillas favoritas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = alSeguirComprando, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Storefront, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ir al Catálogo")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(margen)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(estadoCarrito.items) { item ->
                        TarjetaItemCarrito(
                            item = item,
                            alIncrementar = {
                                if (item.cantidad < item.producto.stock) {
                                    carritoViewModel.modificarCantidad(item.id, item.cantidad + 1)
                                } else {
                                    Toast.makeText(context, "Stock máximo alcanzado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            alDisminuir = {
                                carritoViewModel.modificarCantidad(item.id, item.cantidad - 1)
                            },
                            alEliminar = {
                                carritoViewModel.eliminarItem(item.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Modalidad de Entrega",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Selector de Retiro o Despacho
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = estadoCarrito.tipoEntrega == TipoEntrega.RETIRO_TIENDA,
                                        onClick = { carritoViewModel.cambiarTipoEntrega(TipoEntrega.RETIRO_TIENDA) }
                                    )
                                    Column {
                                        Text("Retiro en tienda (Gratis)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("En sucursal Santiago Centro o punto habilitado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = estadoCarrito.tipoEntrega == TipoEntrega.DESPACHO_DOMICILIO,
                                        onClick = { carritoViewModel.cambiarTipoEntrega(TipoEntrega.DESPACHO_DOMICILIO) }
                                    )
                                    Column {
                                        Text("Despacho a domicilio ($2.000)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Envío rápido dentro de la Región Metropolitana", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                if (estadoCarrito.tipoEntrega == TipoEntrega.DESPACHO_DOMICILIO) {
                                    OutlinedTextField(
                                        value = estadoCarrito.direccionDespacho,
                                        onValueChange = carritoViewModel::cambiarDireccionDespacho,
                                        label = { Text("Dirección de Despacho") },
                                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Tarjeta de Resumen Financiero
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Resumen de Compra", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal productos:")
                                    Text("$${estadoCarrito.subtotal}")
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Costo de despacho:")
                                    Text(if (estadoCarrito.costoDespacho == 0) "Gratis" else "$${estadoCarrito.costoDespacho}")
                                }
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total a pagar:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "$${estadoCarrito.total}",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = VerdeOlivaPrincipal
                                    )
                                }
                            }
                        }
                    }
                }

                // Botón Confirmar Compra
                BotonConCarga(
                    texto = "Confirmar y Procesar Pedido",
                    cargando = estadoPedidos.procesando,
                    alHacerClic = {
                        pedidosViewModel.confirmarPedido(
                            usuarioId = usuario.id,
                            clienteNombre = usuario.nombre,
                            items = estadoCarrito.items,
                            costoDespacho = estadoCarrito.costoDespacho,
                            tipoEntrega = estadoCarrito.tipoEntrega,
                            direccionEntrega = estadoCarrito.direccionDespacho,
                            alExito = { pedido ->
                                carritoViewModel.cargarCarrito()
                                alPedidoConfirmado(pedido)
                            },
                            alError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    icono = Icons.Default.CheckCircle,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TarjetaItemCarrito(
    item: ItemCarrito,
    alIncrementar: () -> Unit,
    alDisminuir: () -> Unit,
    alEliminar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuperficieVariante),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = VerdeOlivaPrincipal, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.producto.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(text = "$${item.producto.precio} c/u · ${item.producto.formato}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                Text(
                    text = "Subtotal: $${item.subtotal}",
                    fontWeight = FontWeight.Bold,
                    color = VerdeOlivaPrincipal,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Controles de cantidad
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = alDisminuir, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Menos", tint = MaterialTheme.colorScheme.primary)
                }
                Text(text = item.cantidad.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                IconButton(onClick = alIncrementar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Más", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = alEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
