package cl.oylfrutossecos.app.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.domain.model.Notificacion
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.domain.model.Usuario
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.NotificacionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    usuario: Usuario,
    notificacionesViewModel: NotificacionesViewModel,
    alNavegarRuta: (String) -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val estado by notificacionesViewModel.uiState.collectAsStateWithLifecycle()

    // Permiso nativo de notificaciones
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            Toast.makeText(context, "Permiso de notificaciones activado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso de notificaciones no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(usuario.id) {
        notificacionesViewModel.cargarNotificaciones(usuario.id, usuario.rol == Rol.ADMINISTRADOR)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avisos y Notificaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        IconButton(onClick = { lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Activar Permiso")
                        }
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
            if (estado.notificaciones.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No tienes notificaciones pendientes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(estado.notificaciones) { notif ->
                        TarjetaNotificacion(
                            notificacion = notif,
                            alHacerClic = {
                                notificacionesViewModel.marcarComoLeida(notif.id, usuario.id, usuario.rol == Rol.ADMINISTRADOR)
                                notif.destinoRuta?.let { ruta -> alNavegarRuta(ruta) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaNotificacion(
    notificacion: Notificacion,
    alHacerClic: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) SuperficieVariante else SuperficieTarjeta
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notificacion.leida) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alHacerClic)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (notificacion.leida) Color.Gray.copy(alpha = 0.2f) else VerdeOlivaClaro),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        notificacion.titulo.contains("stock", ignoreCase = true) -> Icons.Default.WarningAmber
                        notificacion.titulo.contains("pedido", ignoreCase = true) -> Icons.Default.ReceiptLong
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = if (notificacion.leida) TextoSecundario else VerdeOlivaPrincipal,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificacion.titulo,
                        fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = notificacion.fecha,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    lineHeight = 18.sp
                )
            }
        }
    }
}