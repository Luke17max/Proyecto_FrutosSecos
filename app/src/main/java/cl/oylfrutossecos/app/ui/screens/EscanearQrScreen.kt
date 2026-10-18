package cl.oylfrutossecos.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oylfrutossecos.app.domain.model.Producto
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.ui.components.AlertaStockBajoBadge
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.InventarioViewModel
import cl.oylfrutossecos.app.ui.viewmodels.ProductosViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanearQrScreen(
    rolUsuario: Rol,
    productosViewModel: ProductosViewModel,
    inventarioViewModel: InventarioViewModel,
    alVerFicha: (Producto) -> Unit,
    alAjustarInventario: (Producto) -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    var codigoDetectado by remember { mutableStateOf<String?>(null) }
    var productoEncontrado by remember { mutableStateOf<Producto?>(null) }
    var escaneando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var animacionExito by remember { mutableStateOf(false) }

    fun procesarLectura(codigo: String) {
        codigoDetectado = codigo
        mensajeError = null
        productosViewModel.seleccionarProductoPorQr(
            codigoQr = codigo,
            alEncontrar = { prod ->
                productoEncontrado = prod
                animacionExito = true
            },
            alFallar = { err ->
                productoEncontrado = null
                mensajeError = err
                animacionExito = false
            }
        )
    }

    fun abrirEscanerNativo() {
        escaneando = true
        mensajeError = null

        val opciones = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val cliente = GmsBarcodeScanning.getClient(context, opciones)
        cliente.startScan()
            .addOnSuccessListener { barcode ->
                escaneando = false
                val raw = barcode.rawValue ?: ""
                if (raw.isNotBlank()) {
                    procesarLectura(raw)
                } else {
                    mensajeError = "El código QR leído está vacío"
                }
            }
            .addOnCanceledListener {
                escaneando = false
            }
            .addOnFailureListener { e ->
                escaneando = false
                mensajeError = "No se pudo iniciar el escáner: ${e.message}"
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escáner de Productos QR", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Escanea el código del empaque",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Apunta la cámara al código QR de la etiqueta para consultar disponibilidad, precio y gestionar el inventario en terreno.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Visor visual con marco estilo escáner
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SuperficieVariante)
                    .border(3.dp, VerdeOlivaPrincipal, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (animacionExito) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = animacionExito,
                        enter = scaleIn(tween(500)) + fadeIn(tween(500))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(VerdeExito),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("¡Código Reconocido!", fontWeight = FontWeight.Bold, color = VerdeExito)
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Escáner",
                        tint = VerdeOlivaPrincipal,
                        modifier = Modifier.size(110.dp)
                    )
                }
            }

            // Botón Principal: Abrir Escáner Nativo con Cámara
            Button(
                onClick = { abrirEscanerNativo() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear Código con la Cámara", fontWeight = FontWeight.Bold)
            }

            // Simulador de Códigos QR para Pruebas en Emulador
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Simulación Rápida (para emuladores sin cámara física):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { procesarLectura("OYL-ALM-001") },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Almendras", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { procesarLectura("OYL-MIX-003") },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Mix Premium", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { procesarLectura("OYL-CAL-007") },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Calabaza", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Mensaje de Error si el código no coincide
            mensajeError?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = err, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Tarjeta del Producto Reconocido
            productoEncontrado?.let { prod ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(text = prod.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "Código: ${prod.codigoQr} · Formato: ${prod.formato}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            }
                            Text(text = "$${prod.precio}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = VerdeOlivaPrincipal)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = VerdeOlivaClaro,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Stock actual: ${prod.stock} un.",
                                    color = VerdeOlivaOscuro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            if (prod.stockBajo) {
                                AlertaStockBajoBadge(prod.stock)
                            }
                        }

                        Divider()

                        if (rolUsuario == Rol.ADMINISTRADOR) {
                            Button(
                                onClick = {
                                    inventarioViewModel.seleccionarProducto(prod)
                                    alAjustarInventario(prod)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AlmendraDorado),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.SwapVert, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ajustar Inventario de este Producto", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { alVerFicha(prod) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver Ficha y Agregar al Carrito")
                            }
                        }
                    }
                }
            }
        }
    }
}