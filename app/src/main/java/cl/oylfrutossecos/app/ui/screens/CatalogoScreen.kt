package cl.oylfrutossecos.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
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
import cl.oylfrutossecos.app.domain.model.Categoria
import cl.oylfrutossecos.app.domain.model.Producto
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.ui.theme.*
import cl.oylfrutossecos.app.ui.viewmodels.CarritoViewModel
import cl.oylfrutossecos.app.ui.viewmodels.ProductosViewModel

// ============================================================================
// PANTALLA 6: CATÁLOGO DE PRODUCTOS
// ============================================================================
// Roles: Administrador, Cliente
// Presenta frutos secos, semillas y deshidratados organizados por categoría.
// Incluye buscador en vivo, chips de filtro y acciones según rol.
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    rolUsuario: Rol,
    productosViewModel: ProductosViewModel,
    carritoViewModel: CarritoViewModel,
    alSeleccionarProducto: (Producto) -> Unit,
    alEditarProducto: (Producto) -> Unit,
    alIrACarrito: () -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val productos by productosViewModel.productos.collectAsStateWithLifecycle()
    val categoriaSeleccionada by productosViewModel.categoriaSeleccionada.collectAsStateWithLifecycle()
    val busqueda by productosViewModel.busqueda.collectAsStateWithLifecycle()
    val carritoState by carritoViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Productos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (rolUsuario == Rol.CLIENTE) {
                        BadgedBox(
                            badge = {
                                if (carritoState.items.isNotEmpty()) {
                                    Badge { Text(carritoState.cantidadTotalArticulos.toString()) }
                                }
                            },
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            IconButton(onClick = alIrACarrito) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                            }
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
            // Buscador en Vivo
            OutlinedTextField(
                value = busqueda,
                onValueChange = productosViewModel::cambiarBusqueda,
                placeholder = { Text("Buscar frutos secos, semillas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (busqueda.isNotBlank()) {
                        IconButton(onClick = { productosViewModel.cambiarBusqueda("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Chips de Categorías
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Categoria.entries.forEach { cat ->
                    FilterChip(
                        selected = categoriaSeleccionada == cat,
                        onClick = { productosViewModel.seleccionarCategoria(cat) },
                        label = { Text(cat.titulo, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cuadrícula de Productos
            if (productos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No encontramos productos que coincidan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productos) { prod ->
                        TarjetaCatalogo(
                            producto = prod,
                            rol = rolUsuario,
                            alSeleccionar = { alSeleccionarProducto(prod) },
                            alEditar = { alEditarProducto(prod) },
                            alAgregar = {
                                carritoViewModel.agregarAlCarrito(prod.id, 1) { exito, mensaje ->
                                    Toast.makeText(context, mensaje ?: "", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaCatalogo(
    producto: Producto,
    rol: Rol,
    alSeleccionar: () -> Unit,
    alEditar: () -> Unit,
    alAgregar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alSeleccionar)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Contenedor de Fotografía referencial
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SuperficieVariante),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = VerdeOlivaPrincipal,
                    modifier = Modifier.size(46.dp)
                )

                // Badge de Formato
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = producto.formato,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = producto.categoria.titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${producto.precio}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = VerdeOlivaPrincipal
                )
                Text(
                    text = "Stock: ${producto.stock}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (producto.stockBajo) RojoAlerta else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (rol == Rol.ADMINISTRADOR) {
                OutlinedButton(
                    onClick = alEditar,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = alAgregar,
                    enabled = producto.tieneStock,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (producto.tieneStock) "Añadir" else "Sin stock", fontSize = 12.sp)
                }
            }
        }
    }
}
