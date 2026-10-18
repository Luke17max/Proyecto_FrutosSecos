package cl.oylfrutossecos.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.domain.model.Producto
import cl.oylfrutossecos.app.ui.components.AlertaStockBajoBadge
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.InventarioViewModel

// ============================================================================
// PANTALLA 12: GESTIÓN DE INVENTARIO (ADMINISTRADOR)
// ============================================================================
// Rol: Administrador
// Permite consultar existencias, identificar productos con stock bajo y
// registrar movimientos de entrada o salida de inventario en tiempo real.
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionInventarioScreen(
    inventarioViewModel: InventarioViewModel,
    alNavegarAQr: () -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val estado by inventarioViewModel.uiState.collectAsStateWithLifecycle()

    var cantidadAjusteTexto by remember { mutableStateOf("5") }

    val listaFiltrada = remember(estado.productos, estado.soloStockBajo) {
        if (estado.soloStockBajo) {
            estado.productos.filter { it.stockBajo }
        } else {
            estado.productos
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Inventario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = alNavegarAQr) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
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
            // Mensaje de éxito de movimiento
            estado.mensajeConfirmacion?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = VerdeOlivaClaro),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerdeExito)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, style = MaterialTheme.typography.bodySmall, color = VerdeOlivaOscuro, modifier = Modifier.weight(1f))
                        IconButton(onClick = { inventarioViewModel.limpiarMensaje() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = VerdeOlivaOscuro)
                        }
                    }
                }
            }

            // Panel de Ajuste si hay producto seleccionado
            estado.productoSeleccionado?.let { prod ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
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
                                Text(prod.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Código: ${prod.codigoQr} · Formato: ${prod.formato}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            }
                            IconButton(onClick = { inventarioViewModel.seleccionarProducto(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Deseleccionar")
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Stock actual: ${prod.stock} un.", fontWeight = FontWeight.ExtraBold, color = VerdeOlivaPrincipal)
                            if (prod.stockBajo) {
                                AlertaStockBajoBadge(prod.stock)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { inventarioViewModel.registrarMovimiento(esEntrada = true) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdeExito),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Entrada (+1)")
                            }

                            Button(
                                onClick = { inventarioViewModel.registrarMovimiento(esEntrada = false) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RojoAlerta),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salida (-1)")
                            }
                        }
                    }
                }
            }

            // Barra de Búsqueda y Filtro de Stock Bajo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = estado.busqueda,
                    onValueChange = inventarioViewModel::cambiarBusqueda,
                    placeholder = { Text("Buscar en existencias...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = estado.soloStockBajo,
                    onClick = { inventarioViewModel.alternarFiltroStockBajo() },
                    label = { Text("Stock Bajo", fontSize = 11.sp) },
                    leadingIcon = {
                        if (estado.soloStockBajo) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )
            }

            // Lista de existencias
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(listaFiltrada) { prod ->
                    val esSeleccionado = estado.productoSeleccionado?.id == prod.id
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (esSeleccionado) VerdeOlivaClaro else SuperficieTarjeta
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { inventarioViewModel.seleccionarProducto(prod) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("${prod.codigoQr} · ${prod.categoria.titulo}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${prod.stock} un.",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (prod.stockBajo) RojoAlerta else VerdeOlivaPrincipal
                                )
                                if (prod.stockBajo) {
                                    Text("Bajo stock", fontSize = 11.sp, color = RojoAlerta, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
