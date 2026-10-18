package cl.oylfrutossecos.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.ui.components.BotonConCarga
import cl.oylfrutossecos.app.ui.components.CampoTextoFormulario
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.AuthViewModel

// ============================================================================
// PANTALLA 3: INICIO DE SESIÓN
// ============================================================================
// Roles: Administrador, Cliente
// Permite seleccionar el tipo de usuario e ingresar mediante alias y contraseña.
// Incluye selector de rol y credenciales de prueba preconfiguradas.
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    alLoginExitoso: (Rol) -> Unit,
    alIrARegistro: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by viewModel.loginState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acceso al Sistema", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emblema Superior
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(VerdeOlivaClaro),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = VerdeOlivaPrincipal,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "OYL FRUTOS SECOS",
                style = MaterialTheme.typography.titleLarge,
                color = VerdeOlivaPrincipal,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Selector de Rol (Segmented Buttons de Material 3)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = estado.rolSeleccionado == Rol.CLIENTE,
                    onClick = { viewModel.cambiarRolLogin(Rol.CLIENTE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cliente")
                }

                SegmentedButton(
                    selected = estado.rolSeleccionado == Rol.ADMINISTRADOR,
                    onClick = { viewModel.cambiarRolLogin(Rol.ADMINISTRADOR) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Administrador")
                }
            }

            // Tarjeta informativa con credenciales de prueba preconfiguradas
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (estado.rolSeleccionado == Rol.ADMINISTRADOR) {
                            "Cuenta Demo Admin:\nUsuario: admin | Contraseña: admin"
                        } else {
                            "Cuenta Demo Cliente:\nUsuario: ana | Contraseña: 1234"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Error general
            estado.errorGeneral?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Inputs
            CampoTextoFormulario(
                etiqueta = "Usuario o Correo",
                campo = estado.aliasOCorreo,
                alCambiar = viewModel::cambiarAliasLogin,
                icono = Icons.Default.AccountCircle,
                placeholder = if (estado.rolSeleccionado == Rol.ADMINISTRADOR) "admin" else "ana"
            )

            CampoTextoFormulario(
                etiqueta = "Contraseña",
                campo = estado.contrasena,
                alCambiar = viewModel::cambiarContrasenaLogin,
                icono = Icons.Default.Key,
                esContrasena = true,
                placeholder = "Ingresa tu contraseña"
            )

            Spacer(modifier = Modifier.height(8.dp))

            BotonConCarga(
                texto = "Iniciar Sesión",
                cargando = estado.cargando,
                alHacerClic = { viewModel.iniciarSesion(alLoginExitoso) },
                icono = Icons.Default.Login
            )

            if (estado.rolSeleccionado == Rol.CLIENTE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = alIrARegistro) {
                        Text("Regístrate aquí", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
