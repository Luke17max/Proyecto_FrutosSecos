package cl.oylfrutossecos.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.domain.model.EstadoPedido
import cl.oylfrutossecos.app.domain.model.Pedido
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.domain.model.Usuario
import cl.oylfrutossecos.app.ui.components.BadgeEstadoPedido
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.PedidosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialPedidosScreen(
    usuario: Usuario,
    pedidosViewModel: PedidosViewModel,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val estado by pedidosViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(usuario.id) {
        if (usuario.rol == Rol.ADMINISTRADOR) {
            pedidosViewModel.cargarTodosLosPedidosAdmin()
        } else {
            pedidosViewModel.cargarPedidosCliente(usuario.id)
        }
    }

    val pedidosFiltrados = remember(estado.pedidos, estado.filtroEstado) {
        when (estado.filtroEstado) {
            "EN_PROCESO" -> estado.pedidos.filter { it.estado != EstadoPedido.ENTREGADO }
            "COMPLETADOS" -> estado.pedidos.filter { it.estado == EstadoPedido.ENTREGADO }
            else -> estado.pedidos
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (usuario.rol == Rol.ADMINISTRADOR) "Gestión de Pedidos" else "Mis Pedidos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { margen ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(margen)
                .padding(horizontal = 16.dp)
        ) {
            // Filtros de Estado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = estado.filtroEstado == "TODOS",
                    onClick = { pedidosViewModel.cambiarFiltro("TODOS") },
                    label = { Text("Todos (${estado.pedidos.size})") }
                )
                FilterChip(
                    selected = estado.filtroEstado == "EN_PROCESO",
                    onClick = { pedidosViewModel.cambiarFiltro("EN_PROCESO") },
                    label = { Text("En proceso") }
                )
                FilterChip(
                    selected = estado.filtroEstado == "COMPLETADOS",
                    onClick = { pedidosViewModel.cambiarFiltro("COMPLETADOS") },
                    label = { Text("Completados") }
                )
            }

            if (pedidosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (usuario.rol == Rol.ADMINISTRADOR) "No hay solicitudes registradas" else "Aún no has realizado ningún pedido",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(pedidosFiltrados) { pedido ->
                        TarjetaPedido(
                            pedido = pedido,
                            esAdmin = usuario.rol == Rol.ADMINISTRADOR,
                            alCambiarEstado = { nuevoEstado ->
                                pedidosViewModel.actualizarEstadoPedido(
                                    pedidoId = pedido.id,
                                    nuevoEstado = nuevoEstado,
                                    esAdmin = usuario.rol == Rol.ADMINISTRADOR,
                                    usuarioId = usuario.id
                                )
                                Toast.makeText(context, "Estado actualizado a ${nuevoEstado.etiqueta}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPedido(
    pedido: Pedido,
    esAdmin: Boolean,
    alCambiarEstado: (EstadoPedido) -> Unit
) {
    var menuExpandido by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = pedido.numeroPedido, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = VerdeOlivaPrincipal)
                    Text(text = pedido.fecha, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                }
                Text(
                    text = "$${pedido.total}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = VerdeOlivaPrincipal
                )
            }

            if (esAdmin) {
                Text(
                    text = "Cliente: ${pedido.clienteNombre}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    color = MarronNuez
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modalidad: ${pedido.tipoEntrega.etiqueta}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
                BadgeEstadoPedido(pedido.estado)
            }

            // Detalle breve de artículos solicitados
            if (pedido.items.isNotEmpty()) {
                Surface(
                    color = SuperficieVariante,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        pedido.items.forEach { itm ->
                            Text(
                                text = "• ${itm.cantidad}x ${itm.producto.nombre} ($${itm.subtotal})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Controles de cambio de estado (Solo Administrador)
            if (esAdmin) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Actualizar Estado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    Box {
                        OutlinedButton(
                            onClick = { menuExpandido = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Cambiar Estado", fontSize = 12.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = { menuExpandido = false }
                        ) {
                            EstadoPedido.entries.forEach { estadoItem ->
                                DropdownMenuItem(
                                    text = { Text(estadoItem.etiqueta) },
                                    onClick = {
                                        menuExpandido = false
                                        alCambiarEstado(estadoItem)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}