package cl.oylfrutossecos.app.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.oylfrutossecos.app.domain.model.Categoria
import cl.oylfrutossecos.app.domain.model.Producto
import cl.oylfrutossecos.app.ui.components.BotonConCarga
import cl.oylfrutossecos.app.ui.components.CampoTextoFormulario
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.ProductosViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionProductosScreen(
    productosViewModel: ProductosViewModel,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val productos by productosViewModel.productos.collectAsStateWithLifecycle()
    val estadoForm by productosViewModel.formularioProducto.collectAsStateWithLifecycle()

    var mostrandoFormulario by remember { mutableStateOf(false) }
    var uriFotoTemporal by remember { mutableStateOf<Uri?>(null) }

    // Lanzador Nativo 1: Tomar foto con la Cámara del teléfono
    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && uriFotoTemporal != null) {
            productosViewModel.asignarImagenProducto(uriFotoTemporal.toString())
            Toast.makeText(context, "¡Fotografía capturada con éxito!", Toast.LENGTH_SHORT).show()
        }
    }

    // Lanzador Nativo 2: Elegir imagen desde la Galería
    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            productosViewModel.asignarImagenProducto(uri.toString())
            Toast.makeText(context, "Imagen seleccionada de la galería", Toast.LENGTH_SHORT).show()
        }
    }

    fun abrirCamara() {
        try {
            val archivo = File(context.cacheDir, "producto_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "cl.oylfrutossecos.app.fileprovider", archivo)
            uriFotoTemporal = uri
            lanzadorCamara.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo iniciar la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mostrandoFormulario) "Ficha de Producto" else "Gestión de Productos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (mostrandoFormulario) mostrandoFormulario = false else alVolver()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!mostrandoFormulario) {
                FloatingActionButton(
                    onClick = {
                        productosViewModel.iniciarCreacionProducto()
                        mostrandoFormulario = true
                    },
                    containerColor = VerdeOlivaPrincipal,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
                }
            }
        }
    ) { margen ->
        if (mostrandoFormulario) {
            // Formulario de Creación / Edición
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(margen)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (estadoForm.id > 0) "Editar Producto Existente" else "Crear Nuevo Producto en Catálogo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOlivaPrincipal
                )

                // Sección Nativa: Fotografía con Cámara
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuperficieTarjeta),
                            contentAlignment = Alignment.Center
                        ) {
                            if (estadoForm.imagenUri != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerdeExito)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fotografía asociada a la ficha", fontWeight = FontWeight.Bold, color = VerdeExito)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = VerdeOlivaPrincipal, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Sin imagen referencial asignada", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { abrirCamara() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tomar Foto", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { lanzadorGaleria.launch("image/*") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galería", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Error del formulario
                estadoForm.mensajeError?.let { msg ->
                    Text(text = msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                // Campos del Formulario
                CampoTextoFormulario(
                    etiqueta = "Nombre Comercial",
                    campo = estadoForm.nombre,
                    alCambiar = productosViewModel::cambiarNombreProducto,
                    icono = Icons.Default.Label,
                    placeholder = "Ej: Castañas de Cajú Tostadas"
                )

                // Selector de Categoría
                Text("Categoría Oficial:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(Categoria.FRUTOS_SECOS, Categoria.SEMILLAS, Categoria.DESHIDRATADOS).forEach { cat ->
                        FilterChip(
                            selected = estadoForm.categoria == cat,
                            onClick = { productosViewModel.cambiarCategoriaProducto(cat) },
                            label = { Text(cat.titulo, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CampoTextoFormulario(
                        etiqueta = "Precio ($)",
                        campo = estadoForm.precio,
                        alCambiar = productosViewModel::cambiarPrecioProducto,
                        icono = Icons.Default.AttachMoney,
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        placeholder = "2990"
                    )

                    CampoTextoFormulario(
                        etiqueta = "Stock Inicial",
                        campo = estadoForm.stock,
                        alCambiar = productosViewModel::cambiarStockProducto,
                        icono = Icons.Default.Numbers,
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        placeholder = "30"
                    )
                }

                CampoTextoFormulario(
                    etiqueta = "Formato de Venta",
                    campo = estadoForm.formato,
                    alCambiar = productosViewModel::cambiarFormatoProducto,
                    icono = Icons.Default.Scale,
                    placeholder = "250 gr / 500 gr / 1 Kg"
                )

                CampoTextoFormulario(
                    etiqueta = "Descripción Nutricional",
                    campo = estadoForm.descripcion,
                    alCambiar = productosViewModel::cambiarDescripcionProducto,
                    icono = Icons.Default.Description,
                    placeholder = "Propiedades, vitaminas y origen del producto"
                )

                BotonConCarga(
                    texto = if (estadoForm.id > 0) "Guardar Cambios" else "Publicar en Catálogo",
                    cargando = estadoForm.cargando,
                    alHacerClic = {
                        productosViewModel.guardarProducto {
                            Toast.makeText(context, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                            mostrandoFormulario = false
                        }
                    },
                    icono = Icons.Default.Save
                )
            }
        } else {
            // Lista de productos para administración
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(margen)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(productos) { prod ->
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
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VerdeOlivaClaro),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = VerdeOlivaPrincipal)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("${prod.categoria.titulo} · $${prod.precio} · ${prod.formato}", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                                Text(
                                    text = "Stock actual: ${prod.stock} unidades",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (prod.stockBajo) RojoAlerta else VerdeExito
                                )
                            }
                            IconButton(onClick = {
                                productosViewModel.iniciarEdicionProducto(prod)
                                mostrandoFormulario = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}