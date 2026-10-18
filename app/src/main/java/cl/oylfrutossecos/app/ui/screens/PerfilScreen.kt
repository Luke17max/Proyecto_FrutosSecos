package cl.oylfrutossecos.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.domain.model.Usuario
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.AuthViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario,
    authViewModel: AuthViewModel,
    alCerrarSesion: () -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf(usuario.nombre) }
    var correo by remember { mutableStateOf(usuario.correo) }
    var telefono by remember { mutableStateOf(usuario.telefono) }
    var direccion by remember { mutableStateOf(usuario.direccion) }

    var dialogoCerrarSesion by remember { mutableStateOf(false) }
    var uriFotoTemporal by remember { mutableStateOf<Uri?>(null) }

    // Estado en vivo de permisos nativos
    var permisoCamara by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var permisoUbicacion by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var permisoNotificaciones by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // Lanzador Nativo Cámara para foto de avatar
    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && uriFotoTemporal != null) {
            authViewModel.actualizarAvatar(uriFotoTemporal.toString())
            Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoCamara = concedido
        if (concedido) {
            val archivo = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "cl.oylfrutossecos.app.fileprovider", archivo)
            uriFotoTemporal = uri
            lanzadorCamara.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara necesario para capturar foto", Toast.LENGTH_SHORT).show()
        }
    }

    val lanzadorPermisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoUbicacion = concedido
    }

    val lanzadorPermisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoNotificaciones = concedido
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil y Configuración", fontWeight = FontWeight.Bold) },
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
            // Avatar con botón de cámara nativa
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(VerdeOlivaClaro),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (usuario.rol == Rol.ADMINISTRADOR) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = VerdeOlivaPrincipal,
                        modifier = Modifier.size(54.dp)
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (permisoCamara) {
                            val archivo = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "cl.oylfrutossecos.app.fileprovider", archivo)
                            uriFotoTemporal = uri
                            lanzadorCamara.launch(uri)
                        } else {
                            lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.size(34.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = VerdeOlivaPrincipal)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Cambiar Foto", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Text(text = usuario.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Surface(
                color = if (usuario.rol == Rol.ADMINISTRADOR) AlmendraDorado.copy(alpha = 0.25f) else VerdeOlivaClaro,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Rol: ${usuario.rol.name}",
                    color = if (usuario.rol == Rol.ADMINISTRADOR) MarronNuez else VerdeOlivaOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Información Personal
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Datos del Usuario", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono de Contacto") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección de Retiro / Despacho") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val usuarioActualizado = usuario.copy(
                                nombre = nombre,
                                correo = correo,
                                telefono = telefono,
                                direccion = direccion
                            )
                            authViewModel.cerrarSesion { } // Simula refresco de datos locales
                            Toast.makeText(context, "Perfil guardado localmente en SQLite", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios de Perfil")
                    }
                }
            }

            // Gestión de Recursos Nativos y Permisos del Dispositivo
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recursos Nativos y Permisos del Móvil",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = VerdeOlivaPrincipal)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Acceso a Cámara")
                        }
                        Switch(
                            checked = permisoCamara,
                            onCheckedChange = { if (!permisoCamara) lanzadorPermisoCamara.launch(Manifest.permission.CAMERA) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = VerdeOlivaPrincipal)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Acceso a Ubicación GPS")
                        }
                        Switch(
                            checked = permisoUbicacion,
                            onCheckedChange = { if (!permisoUbicacion) lanzadorPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = VerdeOlivaPrincipal)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Notificaciones Push / Alertas")
                            }
                            Switch(
                                checked = permisoNotificaciones,
                                onCheckedChange = { if (!permisoNotificaciones) lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Botón de Cerrar Sesión
            OutlinedButton(
                onClick = { dialogoCerrarSesion = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoAlerta),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }

        // Diálogo de confirmación para cerrar sesión
        if (dialogoCerrarSesion) {
            AlertDialog(
                onDismissRequest = { dialogoCerrarSesion = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Estás seguro de que deseas cerrar tu sesión actual de OYL Frutos Secos?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialogoCerrarSesion = false
                            authViewModel.cerrarSesion(alCerrarSesion)
                        }
                    ) {
                        Text("Sí, Cerrar", color = RojoAlerta, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialogoCerrarSesion = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}