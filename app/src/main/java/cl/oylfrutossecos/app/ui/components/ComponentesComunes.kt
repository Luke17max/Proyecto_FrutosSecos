package cl.oylfrutossecos.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oylfrutossecos.app.domain.model.CampoFormulario
import cl.oylfrutossecos.app.domain.model.EstadoPedido
import cl.oylfrutossecos.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Campo de texto con validación reactiva, íconos y retroalimentación contextual.
 */
@Composable
fun CampoTextoFormulario(
    etiqueta: String,
    campo: CampoFormulario,
    alCambiar: (String) -> Unit,
    icono: ImageVector? = null,
    esContrasena: Boolean = false,
    opcionesTeclado: KeyboardOptions = KeyboardOptions.Default,
    accionesTeclado: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    var contrasenaVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = campo.valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = icono?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = etiqueta,
                    tint = if (campo.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingIcon = if (esContrasena) {
            {
                IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                    Icon(
                        imageVector = if (contrasenaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Alternar contraseña"
                    )
                }
            }
        } else if (campo.error != null) {
            {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        } else null,
        isError = campo.error != null,
        supportingText = {
            AnimatedVisibility(visible = campo.error != null) {
                campo.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        visualTransformation = if (esContrasena && !contrasenaVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = opcionesTeclado,
        keyboardActions = accionesTeclado,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Botón con estado animado de carga (Loader) para feedback durante procesos asíncronos.
 */
@Composable
fun BotonConCarga(
    texto: String,
    cargando: Boolean,
    alHacerClic: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icono: ImageVector? = null,
    colorFondo: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = alHacerClic,
        enabled = habilitado && !cargando,
        colors = ButtonDefaults.buttonColors(containerColor = colorFondo),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Procesando...", style = MaterialTheme.typography.titleMedium)
        } else {
            if (icono != null) {
                Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(texto, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Badge para el estado de un pedido en curso o finalizado.
 */
@Composable
fun BadgeEstadoPedido(estado: EstadoPedido) {
    val (colorFondo, colorTexto, icono) = when (estado) {
        EstadoPedido.PENDIENTE_VALIDACION -> Triple(
            Color(0xFFFFE0B2),
            NaranjaPendiente,
            Icons.Default.HourglassTop
        )
        EstadoPedido.CONFIRMADO -> Triple(
            Color(0xFFB3E5FC),
            AzulProceso,
            Icons.Default.FactCheck
        )
        EstadoPedido.PREPARADO -> Triple(
            Color(0xFFE1BEE7),
            Color(0xFF6A1B9A),
            Icons.Default.Inventory2
        )
        EstadoPedido.ENTREGADO -> Triple(
            Color(0xFFC8E6C9),
            VerdeExito,
            Icons.Default.CheckCircle
        )
    }

    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = estado.etiqueta,
                color = colorTexto,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Indicador animado de stock bajo.
 */
@Composable
fun AlertaStockBajoBadge(stock: Int) {
    val colorAnimado by animateColorAsState(
        targetValue = if (stock <= 5) RojoAlerta else Color(0xFFF57C00),
        animationSpec = tween(600),
        label = "stockColor"
    )

    Surface(
        color = colorAnimado.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = colorAnimado, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Stock bajo: $stock un.",
                color = colorAnimado,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Componente reactivo para renderizar imágenes de productos (desde ruta interna o URI)
 * con fallback a ícono temático y decodificación asíncrona segura.
 */
@Composable
fun ImagenProducto(
    imagenUri: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    iconoPorDefecto: ImageVector = Icons.Default.Eco,
    colorIconoPorDefecto: Color = VerdeOlivaPrincipal,
    tamanoIconoPorDefecto: Dp = 32.dp
) {
    val context = LocalContext.current
    var bitmapState by remember(imagenUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imagenUri) {
        if (!imagenUri.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    // 1. Intentar resolver como recurso drawable empaquetado (ej: "prod_almendras_naturales")
                    val resId = context.resources.getIdentifier(imagenUri, "drawable", context.packageName)
                    if (resId != 0) {
                        bitmapState = BitmapFactory.decodeResource(context.resources, resId)
                        return@withContext
                    }

                    // 2. Si no es un drawable por nombre, resolver como URI (content, file, android.resource o ruta de archivo)
                    val uri = Uri.parse(imagenUri)
                    var inputStream: InputStream? = null
                    var rutaArchivo: String? = null

                    if (uri.scheme == "content" || uri.scheme == "android.resource") {
                        inputStream = context.contentResolver.openInputStream(uri)
                    } else {
                        val path = uri.path ?: imagenUri
                        val archivo = File(path)
                        if (archivo.exists()) {
                            rutaArchivo = archivo.absolutePath
                            inputStream = archivo.inputStream()
                        } else {
                            inputStream = context.contentResolver.openInputStream(uri)
                        }
                    }

                    val bitmapDecodificado = inputStream?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }

                    // Corrección de rotación EXIF si la foto proviene de la cámara física
                    val bitmapFinal = if (bitmapDecodificado != null && rutaArchivo != null) {
                        try {
                            val exif = ExifInterface(rutaArchivo)
                            val orientacion = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                            val grados = when (orientacion) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                                else -> 0f
                            }
                            if (grados != 0f) {
                                val matriz = Matrix().apply { postRotate(grados) }
                                Bitmap.createBitmap(
                                    bitmapDecodificado,
                                    0,
                                    0,
                                    bitmapDecodificado.width,
                                    bitmapDecodificado.height,
                                    matriz,
                                    true
                                )
                            } else {
                                bitmapDecodificado
                            }
                        } catch (_: Exception) {
                            bitmapDecodificado
                        }
                    } else {
                        bitmapDecodificado
                    }

                    bitmapState = bitmapFinal
                } catch (e: Exception) {
                    e.printStackTrace()
                    bitmapState = null
                }
            }
        } else {
            bitmapState = null
        }
    }

    val bmp = bitmapState
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconoPorDefecto,
                contentDescription = contentDescription,
                tint = colorIconoPorDefecto,
                modifier = Modifier.size(tamanoIconoPorDefecto)
            )
        }
    }
}