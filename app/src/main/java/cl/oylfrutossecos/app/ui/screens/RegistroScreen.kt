package cl.oylfrutossecos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.ui.components.BotonConCarga
import cl.oylfrutossecos.app.ui.components.CampoTextoFormulario
import cl.oylfrutossecos.app.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    alRegistrarExitoso: () -> Unit,
    alIrALogin: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by viewModel.registroState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta de Cliente", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Regístrate para comprar",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Ingresa tus datos para gestionar tus pedidos y consultar tu catálogo local.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mensaje de Error General si existiera
            estado.errorGeneral?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Campos del Formulario
            CampoTextoFormulario(
                etiqueta = "Nombre Completo",
                campo = estado.nombre,
                alCambiar = viewModel::cambiarNombreRegistro,
                icono = Icons.Default.Person,
                placeholder = "Ej: Carlos Tapia"
            )

            CampoTextoFormulario(
                etiqueta = "Alias (Usuario)",
                campo = estado.alias,
                alCambiar = viewModel::cambiarAliasRegistro,
                icono = Icons.Default.AlternateEmail,
                placeholder = "Ej: carlost"
            )

            CampoTextoFormulario(
                etiqueta = "Correo Electrónico",
                campo = estado.correo,
                alCambiar = viewModel::cambiarCorreoRegistro,
                icono = Icons.Default.Email,
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = "carlos@correo.cl"
            )

            CampoTextoFormulario(
                etiqueta = "Teléfono de Contacto",
                campo = estado.telefono,
                alCambiar = viewModel::cambiarTelefonoRegistro,
                icono = Icons.Default.Phone,
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Phone),
                placeholder = "+56 9 8765 4321"
            )

            CampoTextoFormulario(
                etiqueta = "Dirección de Entrega",
                campo = estado.direccion,
                alCambiar = viewModel::cambiarDireccionRegistro,
                icono = Icons.Default.Home,
                placeholder = "Calle y número, Comuna"
            )

            CampoTextoFormulario(
                etiqueta = "Contraseña",
                campo = estado.contrasena,
                alCambiar = viewModel::cambiarContrasenaRegistro,
                icono = Icons.Default.Lock,
                esContrasena = true,
                placeholder = "Mínimo 4 caracteres"
            )

            CampoTextoFormulario(
                etiqueta = "Confirmar Contraseña",
                campo = estado.confirmarContrasena,
                alCambiar = viewModel::cambiarConfirmarContrasenaRegistro,
                icono = Icons.Default.LockClock,
                esContrasena = true,
                placeholder = "Repite tu contraseña"
            )

            Spacer(modifier = Modifier.height(10.dp))

            BotonConCarga(
                texto = "Registrarme",
                cargando = estado.cargando,
                alHacerClic = { viewModel.registrar(alRegistrarExitoso) },
                icono = Icons.Default.PersonAdd
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Ya tienes una cuenta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = alIrALogin) {
                    Text("Inicia sesión aquí", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}