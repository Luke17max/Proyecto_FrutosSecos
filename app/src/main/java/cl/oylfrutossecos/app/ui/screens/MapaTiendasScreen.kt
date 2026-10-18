package cl.oylfrutossecos.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.domain.model.PuntoRetiro
import cl.oylfrutossecos.app.ui.theme.SuperficieTarjeta
import cl.oylfrutossecos.app.ui.theme.VerdeOlivaPrincipal
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// ============================================================================
// PANTALLA 5: MAPA DE TIENDAS Y PUNTOS DE RETIRO
// ============================================================================
// Rol: Cliente
// Recurso Nativo: Mapas OpenStreetMap (osmdroid) y Localización GPS.
// Muestra la ubicación de la tienda central y los puntos de retiro habilitados
// con horarios, dirección y geolocalización en tiempo real.
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaTiendasScreen(
    repository: OylRepository,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val puntos = remember { repository.obtenerPuntosDeRetiro() }
    var puntoSeleccionado by remember { mutableStateOf<PuntoRetiro?>(puntos.firstOrNull()) }
    var vistaMapa by remember { mutableStateOf<MapView?>(null) }
    var capaMiUbicacion by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    // Inicializar configuración requerida por osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    // Permisos nativos de ubicación
    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos.values.any { it }
        if (concedido) {
            capaMiUbicacion?.enableMyLocation()
            Toast.makeText(context, "GPS activado. Centrando en tu ubicación...", Toast.LENGTH_SHORT).show()
            capaMiUbicacion?.myLocation?.let { loc ->
                vistaMapa?.controller?.animateTo(loc)
                vistaMapa?.controller?.setZoom(16.0)
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    fun centrarEnMiUbicacion() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            capaMiUbicacion?.enableMyLocation()
            val miLoc = capaMiUbicacion?.myLocation
            if (miLoc != null) {
                vistaMapa?.controller?.animateTo(miLoc)
                vistaMapa?.controller?.setZoom(16.0)
            } else {
                Toast.makeText(context, "Obteniendo señal GPS...", Toast.LENGTH_SHORT).show()
            }
        } else {
            lanzadorPermisos.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tiendas y Puntos de Retiro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { margen ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(margen)
        ) {
            // Componente de Mapa Nativo embebido en Compose
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(14.0)
                        controller.setCenter(GeoPoint(-33.4440, -70.6540)) // Centro de Santiago

                        // Capa de Mi Ubicación
                        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        overlays.add(overlay)
                        capaMiUbicacion = overlay

                        // Agregar Marcadores de tiendas y retiros
                        for (p in puntos) {
                            val marker = Marker(this).apply {
                                position = GeoPoint(p.latitud, p.longitud)
                                title = p.nombre
                                snippet = p.direccion
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                setOnMarkerClickListener { _, _ ->
                                    puntoSeleccionado = p
                                    controller.animateTo(position)
                                    true
                                }
                            }
                            overlays.add(marker)
                        }

                        vistaMapa = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Controles flotantes de zoom y GPS
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { vistaMapa?.controller?.zoomIn() },
                    shape = CircleShape,
                    containerColor = SuperficieTarjeta,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Acercar")
                }

                FloatingActionButton(
                    onClick = { vistaMapa?.controller?.zoomOut() },
                    shape = CircleShape,
                    containerColor = SuperficieTarjeta,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Alejar")
                }

                FloatingActionButton(
                    onClick = { centrarEnMiUbicacion() },
                    shape = CircleShape,
                    containerColor = VerdeOlivaPrincipal,
                    contentColor = Color.White,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
                }
            }

            // Tarjeta inferior informativa del punto de retiro seleccionado
            puntoSeleccionado?.let { punto ->
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = punto.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VerdeOlivaPrincipal
                            )
                            if (punto.esTiendaPrincipal) {
                                Surface(
                                    color = VerdeOlivaPrincipal.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Tienda Central",
                                        color = VerdeOlivaPrincipal,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = punto.direccion, style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = punto.horario, style = MaterialTheme.typography.bodySmall)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = punto.telefono, style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = {
                                vistaMapa?.controller?.animateTo(GeoPoint(punto.latitud, punto.longitud))
                                vistaMapa?.controller?.setZoom(16.0)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cómo llegar a este punto")
                        }
                    }
                }
            }
        }
    }
}
