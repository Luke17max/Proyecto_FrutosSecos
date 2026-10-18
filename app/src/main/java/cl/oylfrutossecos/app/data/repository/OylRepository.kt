package cl.oylfrutossecos.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import cl.oylfrutossecos.app.data.local.OylDbHelper
import cl.oylfrutossecos.app.domain.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
// REPOSITORIO DE DATOS - OYL FRUTOS SECOS
// ============================================================================
// Provee una capa de abstracción entre la base de datos SQLite y la lógica
// de presentación (ViewModels), aplicando el patrón Repository.
// ============================================================================

class OylRepository private constructor(context: Context) {

    private val dbHelper = OylDbHelper.obtenerInstancia(context)

    companion object {
        @Volatile
        private var INSTANCIA: OylRepository? = null

        fun obtenerInstancia(context: Context): OylRepository {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: OylRepository(context).also { INSTANCIA = it }
            }
        }
    }

    // ------------------------------------------------------------------------
    // 1. AUTENTICACIÓN Y SESIONES
    // ------------------------------------------------------------------------

    fun iniciarSesion(aliasOCorreo: String, contrasena: String): Usuario? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT * FROM usuarios 
            WHERE (LOWER(alias) = LOWER(?) OR LOWER(correo) = LOWER(?)) 
            AND contrasena = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(aliasOCorreo.trim(), aliasOCorreo.trim(), contrasena))

        return cursor.use {
            if (it.moveToFirst()) {
                val usuario = cursorAUsuario(it)
                guardarSesionActiva(usuario.id)
                usuario
            } else null
        }
    }

    fun registrarCliente(
        nombre: String,
        alias: String,
        correo: String,
        telefono: String,
        direccion: String,
        contrasena: String
    ): Result<Usuario> {
        val db = dbHelper.writableDatabase

        // Verificar si alias o correo ya existen
        val cursorExistente = db.rawQuery(
            "SELECT id FROM usuarios WHERE LOWER(alias) = LOWER(?) OR LOWER(correo) = LOWER(?)",
            arrayOf(alias.trim(), correo.trim())
        )
        val existe = cursorExistente.use { it.count > 0 }
        if (existe) {
            return Result.failure(Exception("El alias o correo ya se encuentra registrado"))
        }

        val valores = ContentValues().apply {
            put("nombre", nombre.trim())
            put("alias", alias.trim())
            put("correo", correo.trim())
            put("telefono", telefono.trim())
            put("direccion", direccion.trim())
            put("contrasena", contrasena)
            put("rol", Rol.CLIENTE.name)
        }

        val id = db.insert("usuarios", null, valores)
        if (id == -1L) {
            return Result.failure(Exception("Error al registrar cliente en la base de datos"))
        }

        val nuevoUsuario = Usuario(
            id = id,
            nombre = nombre.trim(),
            alias = alias.trim(),
            correo = correo.trim(),
            telefono = telefono.trim(),
            direccion = direccion.trim(),
            contrasena = contrasena,
            rol = Rol.CLIENTE
        )
        guardarSesionActiva(id)
        return Result.success(nuevoUsuario)
    }

    fun obtenerUsuarioActivo(): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT u.* FROM usuarios u
            INNER JOIN sesion_activa s ON u.id = s.usuario_id
            LIMIT 1
            """.trimIndent(),
            null
        )
        return cursor.use {
            if (it.moveToFirst()) cursorAUsuario(it) else null
        }
    }

    private fun guardarSesionActiva(usuarioId: Long) {
        val db = dbHelper.writableDatabase
        db.delete("sesion_activa", null, null)
        val cv = ContentValues().apply {
            put("id", 1)
            put("usuario_id", usuarioId)
        }
        db.insert("sesion_activa", null, cv)
    }

    fun cerrarSesion() {
        val db = dbHelper.writableDatabase
        db.delete("sesion_activa", null, null)
    }

    fun actualizarPerfil(usuario: Usuario): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("nombre", usuario.nombre)
            put("correo", usuario.correo)
            put("telefono", usuario.telefono)
            put("direccion", usuario.direccion)
            if (usuario.avatarUri != null) {
                put("avatar_uri", usuario.avatarUri)
            }
        }
        return db.update("usuarios", cv, "id = ?", arrayOf(usuario.id.toString())) > 0
    }

    // ------------------------------------------------------------------------
    // 2. CATÁLOGO DE PRODUCTOS
    // ------------------------------------------------------------------------

    fun obtenerProductos(categoria: Categoria = Categoria.TODOS, busqueda: String = ""): List<Producto> {
        val db = dbHelper.readableDatabase
        val clausulas = mutableListOf<String>()
        val argumentos = mutableListOf<String>()

        if (categoria != Categoria.TODOS) {
            clausulas.add("categoria = ?")
            argumentos.add(categoria.name)
        }

        if (busqueda.isNotBlank()) {
            clausulas.add("(LOWER(nombre) LIKE ? OR LOWER(descripcion) LIKE ?)")
            argumentos.add("%${busqueda.lowercase()}%")
            argumentos.add("%${busqueda.lowercase()}%")
        }

        val where = if (clausulas.isNotEmpty()) clausulas.joinToString(" AND ") else null
        val cursor = db.query(
            "productos",
            null,
            where,
            if (argumentos.isNotEmpty()) argumentos.toTypedArray() else null,
            null,
            null,
            "nombre ASC"
        )

        val lista = mutableListOf<Producto>()
        cursor.use {
            while (it.moveToNext()) {
                lista.add(cursorAProducto(it))
            }
        }
        return lista
    }

    fun obtenerProductoPorId(id: Long): Producto? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("productos", null, "id = ?", arrayOf(id.toString()), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) cursorAProducto(it) else null
        }
    }

    fun obtenerProductoPorQr(codigoQr: String): Producto? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("productos", null, "codigo_qr = ?", arrayOf(codigoQr.trim()), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) cursorAProducto(it) else null
        }
    }

    fun guardarOActualizarProducto(producto: Producto): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("codigo_qr", producto.codigoQr)
            put("nombre", producto.nombre)
            put("categoria", producto.categoria.name)
            put("precio", producto.precio)
            put("stock", producto.stock)
            put("formato", producto.formato)
            put("descripcion", producto.descripcion)
            put("imagen_uri", producto.imagenUri)
            put("activo", if (producto.activo) 1 else 0)
        }

        return if (producto.id > 0) {
            db.update("productos", cv, "id = ?", arrayOf(producto.id.toString()))
            producto.id
        } else {
            db.insert("productos", null, cv)
        }
    }

    fun actualizarStock(productoId: Long, nuevoStock: Int): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("stock", nuevoStock)
        }
        val filas = db.update("productos", cv, "id = ?", arrayOf(productoId.toString()))
        return filas > 0
    }

    // ------------------------------------------------------------------------
    // 3. GESTIÓN DEL CARRITO (Aislado por cliente)
    // ------------------------------------------------------------------------

    fun obtenerCarrito(usuarioId: Long): List<ItemCarrito> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT c.id as item_id, c.usuario_id, c.cantidad, p.* 
            FROM carrito c
            INNER JOIN productos p ON c.producto_id = p.id
            WHERE c.usuario_id = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(usuarioId.toString()))
        val items = mutableListOf<ItemCarrito>()
        cursor.use {
            while (it.moveToNext()) {
                val itemId = it.getLong(it.getColumnIndexOrThrow("item_id"))
                val cantidad = it.getInt(it.getColumnIndexOrThrow("cantidad"))
                val producto = cursorAProducto(it)
                items.add(ItemCarrito(id = itemId, usuarioId = usuarioId, producto = producto, cantidad = cantidad))
            }
        }
        return items
    }

    fun agregarAlCarrito(usuarioId: Long, productoId: Long, cantidad: Int = 1): Boolean {
        val db = dbHelper.writableDatabase

        // Verificar stock disponible del producto
        val prod = obtenerProductoPorId(productoId) ?: return false

        val cursor = db.query(
            "carrito",
            arrayOf("id", "cantidad"),
            "usuario_id = ? AND producto_id = ?",
            arrayOf(usuarioId.toString(), productoId.toString()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                val itemId = it.getLong(0)
                val cantidadActual = it.getInt(1)
                val nuevaCantidad = cantidadActual + cantidad
                if (nuevaCantidad > prod.stock) return false

                val cv = ContentValues().apply { put("cantidad", nuevaCantidad) }
                db.update("carrito", cv, "id = ?", arrayOf(itemId.toString())) > 0
            } else {
                if (cantidad > prod.stock) return false
                val cv = ContentValues().apply {
                    put("usuario_id", usuarioId)
                    put("producto_id", productoId)
                    put("cantidad", cantidad)
                }
                db.insert("carrito", null, cv) > 0
            }
        }
    }

    fun modificarCantidadCarrito(itemId: Long, nuevaCantidad: Int): Boolean {
        val db = dbHelper.writableDatabase
        if (nuevaCantidad <= 0) {
            return db.delete("carrito", "id = ?", arrayOf(itemId.toString())) > 0
        }
        val cv = ContentValues().apply { put("cantidad", nuevaCantidad) }
        return db.update("carrito", cv, "id = ?", arrayOf(itemId.toString())) > 0
    }

    fun eliminarItemCarrito(itemId: Long): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete("carrito", "id = ?", arrayOf(itemId.toString())) > 0
    }

    fun limpiarCarrito(usuarioId: Long) {
        val db = dbHelper.writableDatabase
        db.delete("carrito", "usuario_id = ?", arrayOf(usuarioId.toString()))
    }

    // ------------------------------------------------------------------------
    // 4. TRANSACCIONES Y PEDIDOS (Control estricto de Stock)
    // ------------------------------------------------------------------------

    fun procesarPedido(
        usuarioId: Long,
        clienteNombre: String,
        items: List<ItemCarrito>,
        costoDespacho: Int,
        tipoEntrega: TipoEntrega,
        direccionEntrega: String
    ): Result<Pedido> {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Validar disponibilidad de stock de todos los artículos de manera transaccional
            for (item in items) {
                val cursorStock = db.query(
                    "productos",
                    arrayOf("stock", "nombre"),
                    "id = ?",
                    arrayOf(item.producto.id.toString()),
                    null, null, null
                )
                if (cursorStock.moveToFirst()) {
                    val stockActual = cursorStock.getInt(0)
                    val nombreProd = cursorStock.getString(1)
                    if (stockActual < item.cantidad) {
                        cursorStock.close()
                        return Result.failure(
                            Exception("Stock insuficiente para $nombreProd (Disponibles: $stockActual, Solicitados: ${item.cantidad})")
                        )
                    }
                } else {
                    cursorStock.close()
                    return Result.failure(Exception("Producto ID ${item.producto.id} no encontrado"))
                }
                cursorStock.close()
            }

            // 2. Descontar stock de cada producto
            for (item in items) {
                db.execSQL(
                    "UPDATE productos SET stock = stock - ? WHERE id = ?",
                    arrayOf(item.cantidad, item.producto.id)
                )
            }

            // 3. Crear cabecera del Pedido
            val numeroPedido = "#" + (10000 + (Math.random() * 90000).toInt()).toString()
            val totalSubtotal = items.sumOf { it.subtotal }
            val totalConDespacho = totalSubtotal + costoDespacho
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault())
            val fechaActual = formatoFecha.format(Date())

            val cvPedido = ContentValues().apply {
                put("numero_pedido", numeroPedido)
                put("usuario_id", usuarioId)
                put("cliente_nombre", clienteNombre)
                put("total", totalConDespacho)
                put("costo_despacho", costoDespacho)
                put("tipo_entrega", tipoEntrega.name)
                put("direccion_entrega", direccionEntrega)
                put("estado", EstadoPedido.PENDIENTE_VALIDACION.name)
                put("fecha", fechaActual)
                put("timestamp", System.currentTimeMillis())
            }

            val pedidoId = db.insert("pedidos", null, cvPedido)
            if (pedidoId == -1L) {
                return Result.failure(Exception("No se pudo registrar la cabecera del pedido"))
            }

            // 4. Registrar los ítems del pedido
            for (item in items) {
                val cvItem = ContentValues().apply {
                    put("pedido_id", pedidoId)
                    put("producto_id", item.producto.id)
                    put("nombre_producto", item.producto.nombre)
                    put("precio_unitario", item.producto.precio)
                    put("cantidad", item.cantidad)
                    put("subtotal", item.subtotal)
                }
                db.insert("pedido_items", null, cvItem)
            }

            // 5. Limpiar el carrito del cliente
            db.delete("carrito", "usuario_id = ?", arrayOf(usuarioId.toString()))

            // 6. Generar Notificación para el Admin sobre nuevo pedido
            val cvNotifAdmin = ContentValues().apply {
                put("usuario_id", null as Long?)
                put("titulo", "Nuevo pedido recibido")
                put("mensaje", "Pedido $numeroPedido por $totalConDespacho de $clienteNombre.")
                put("fecha", fechaActual)
                put("leida", 0)
                put("destino_ruta", "pedidos")
            }
            db.insert("notificaciones", null, cvNotifAdmin)

            db.setTransactionSuccessful()

            val pedidoCreado = Pedido(
                id = pedidoId,
                numeroPedido = numeroPedido,
                usuarioId = usuarioId,
                clienteNombre = clienteNombre,
                items = items,
                total = totalConDespacho,
                costoDespacho = costoDespacho,
                tipoEntrega = tipoEntrega,
                direccionEntrega = direccionEntrega,
                estado = EstadoPedido.PENDIENTE_VALIDACION,
                fecha = fechaActual
            )
            return Result.success(pedidoCreado)
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerPedidosUsuario(usuarioId: Long): List<Pedido> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "pedidos",
            null,
            "usuario_id = ?",
            arrayOf(usuarioId.toString()),
            null, null,
            "id DESC"
        )
        val lista = mutableListOf<Pedido>()
        cursor.use {
            while (it.moveToNext()) {
                val pedidoId = it.getLong(it.getColumnIndexOrThrow("id"))
                val items = obtenerItemsDePedido(pedidoId)
                lista.add(cursorAPedido(it, items))
            }
        }
        return lista
    }

    fun obtenerTodosLosPedidos(): List<Pedido> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "pedidos",
            null,
            null,
            null,
            null, null,
            "id DESC"
        )
        val lista = mutableListOf<Pedido>()
        cursor.use {
            while (it.moveToNext()) {
                val pedidoId = it.getLong(it.getColumnIndexOrThrow("id"))
                val items = obtenerItemsDePedido(pedidoId)
                lista.add(cursorAPedido(it, items))
            }
        }
        return lista
    }

    private fun obtenerItemsDePedido(pedidoId: Long): List<ItemCarrito> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("pedido_items", null, "pedido_id = ?", arrayOf(pedidoId.toString()), null, null, null)
        val items = mutableListOf<ItemCarrito>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id"))
                val productoId = it.getLong(it.getColumnIndexOrThrow("producto_id"))
                val nombre = it.getString(it.getColumnIndexOrThrow("nombre_producto"))
                val precio = it.getInt(it.getColumnIndexOrThrow("precio_unitario"))
                val cantidad = it.getInt(it.getColumnIndexOrThrow("cantidad"))

                val prodSimulado = Producto(
                    id = productoId,
                    codigoQr = "OYL-ITEM-$productoId",
                    nombre = nombre,
                    categoria = Categoria.FRUTOS_SECOS,
                    precio = precio,
                    stock = 0,
                    formato = "250 gr",
                    descripcion = ""
                )
                items.add(ItemCarrito(id = id, usuarioId = 0, producto = prodSimulado, cantidad = cantidad))
            }
        }
        return items
    }

    fun actualizarEstadoPedido(pedidoId: Long, nuevoEstado: EstadoPedido): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("estado", nuevoEstado.name)
        }
        val filas = db.update("pedidos", cv, "id = ?", arrayOf(pedidoId.toString()))

        if (filas > 0) {
            // Notificar al cliente sobre el cambio de estado de su solicitud
            val cursorPedido = db.query("pedidos", arrayOf("usuario_id", "numero_pedido"), "id = ?", arrayOf(pedidoId.toString()), null, null, null)
            cursorPedido.use {
                if (it.moveToFirst()) {
                    val userId = it.getLong(0)
                    val numPedido = it.getString(1)
                    val fechaActual = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()).format(Date())
                    val cvNotif = ContentValues().apply {
                        put("usuario_id", userId)
                        put("titulo", "Pedido $nuevoEstado")
                        put("mensaje", "Tu pedido $numPedido ahora se encuentra en estado: ${nuevoEstado.etiqueta}.")
                        put("fecha", fechaActual)
                        put("leida", 0)
                        put("destino_ruta", "pedidos")
                    }
                    db.insert("notificaciones", null, cvNotif)
                }
            }
        }
        return filas > 0
    }

    // ------------------------------------------------------------------------
    // 5. NOTIFICACIONES
    // ------------------------------------------------------------------------

    fun obtenerNotificaciones(usuarioId: Long, esAdmin: Boolean): List<Notificacion> {
        val db = dbHelper.readableDatabase
        val cursor = if (esAdmin) {
            db.query("notificaciones", null, "usuario_id IS NULL OR usuario_id = ?", arrayOf(usuarioId.toString()), null, null, "id DESC")
        } else {
            db.query("notificaciones", null, "usuario_id = ?", arrayOf(usuarioId.toString()), null, null, "id DESC")
        }

        val lista = mutableListOf<Notificacion>()
        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    Notificacion(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        usuarioId = if (it.isNull(it.getColumnIndexOrThrow("usuario_id"))) null else it.getLong(it.getColumnIndexOrThrow("usuario_id")),
                        titulo = it.getString(it.getColumnIndexOrThrow("titulo")),
                        mensaje = it.getString(it.getColumnIndexOrThrow("mensaje")),
                        fecha = it.getString(it.getColumnIndexOrThrow("fecha")),
                        leida = it.getInt(it.getColumnIndexOrThrow("leida")) == 1,
                        destinoRuta = it.getString(it.getColumnIndexOrThrow("destino_ruta"))
                    )
                )
            }
        }
        return lista
    }

    fun marcarNotificacionLeida(id: Long) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply { put("leida", 1) }
        db.update("notificaciones", cv, "id = ?", arrayOf(id.toString()))
    }

    // ------------------------------------------------------------------------
    // 6. PUNTOS DE RETIRO Y TIENDA (Para mapa OSMDroid)
    // ------------------------------------------------------------------------

    fun obtenerPuntosDeRetiro(): List<PuntoRetiro> {
        return listOf(
            PuntoRetiro(
                id = 1,
                nombre = "Tienda OYL - Santiago Centro",
                direccion = "Av. Libertador Bernardo O'Higgins 1234, Santiago, RM",
                horario = "Abierta · 8:00 - 19:00",
                telefono = "+56 9 1234 1234",
                latitud = -33.4440,
                longitud = -70.6540,
                esTiendaPrincipal = true
            ),
            PuntoRetiro(
                id = 2,
                nombre = "Punto de Retiro Providencia",
                direccion = "Av. Providencia 2124 (Galería Comercial)",
                horario = "Lunes a Viernes · 9:30 - 18:30",
                telefono = "+56 9 8765 4321",
                latitud = -33.4248,
                longitud = -70.6110,
                esTiendaPrincipal = false
            ),
            PuntoRetiro(
                id = 3,
                nombre = "Punto de Retiro Las Condes",
                direccion = "Av. Apoquindo 4411 (Sector Escuela Militar)",
                horario = "Lunes a Sábado · 10:00 - 20:00",
                telefono = "+56 9 5555 1234",
                latitud = -33.4116,
                longitud = -70.5794,
                esTiendaPrincipal = false
            ),
            PuntoRetiro(
                id = 4,
                nombre = "Punto de Retiro Maipú",
                direccion = "Av. Pajaritos 3020 (Plaza Maipú)",
                horario = "Lunes a Viernes · 9:00 - 18:00",
                telefono = "+56 9 4444 9876",
                latitud = -33.5103,
                longitud = -70.7573,
                esTiendaPrincipal = false
            )
        )
    }

    // ------------------------------------------------------------------------
    // MAPPERS PRIVADOS DE CURSOR
    // ------------------------------------------------------------------------

    private fun cursorAUsuario(c: Cursor): Usuario {
        return Usuario(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            nombre = c.getString(c.getColumnIndexOrThrow("nombre")),
            alias = c.getString(c.getColumnIndexOrThrow("alias")),
            correo = c.getString(c.getColumnIndexOrThrow("correo")),
            telefono = c.getString(c.getColumnIndexOrThrow("telefono")),
            direccion = c.getString(c.getColumnIndexOrThrow("direccion")),
            contrasena = c.getString(c.getColumnIndexOrThrow("contrasena")),
            rol = Rol.valueOf(c.getString(c.getColumnIndexOrThrow("rol"))),
            avatarUri = c.getString(c.getColumnIndexOrThrow("avatar_uri"))
        )
    }

    private fun cursorAProducto(c: Cursor): Producto {
        return Producto(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            codigoQr = c.getString(c.getColumnIndexOrThrow("codigo_qr")),
            nombre = c.getString(c.getColumnIndexOrThrow("nombre")),
            categoria = Categoria.valueOf(c.getString(c.getColumnIndexOrThrow("categoria"))),
            precio = c.getInt(c.getColumnIndexOrThrow("precio")),
            stock = c.getInt(c.getColumnIndexOrThrow("stock")),
            formato = c.getString(c.getColumnIndexOrThrow("formato")),
            descripcion = c.getString(c.getColumnIndexOrThrow("descripcion")),
            imagenUri = c.getString(c.getColumnIndexOrThrow("imagen_uri")),
            activo = c.getInt(c.getColumnIndexOrThrow("activo")) == 1
        )
    }

    private fun cursorAPedido(c: Cursor, items: List<ItemCarrito>): Pedido {
        return Pedido(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            numeroPedido = c.getString(c.getColumnIndexOrThrow("numero_pedido")),
            usuarioId = c.getLong(c.getColumnIndexOrThrow("usuario_id")),
            clienteNombre = c.getString(c.getColumnIndexOrThrow("cliente_nombre")),
            items = items,
            total = c.getInt(c.getColumnIndexOrThrow("total")),
            costoDespacho = c.getInt(c.getColumnIndexOrThrow("costo_despacho")),
            tipoEntrega = TipoEntrega.valueOf(c.getString(c.getColumnIndexOrThrow("tipo_entrega"))),
            direccionEntrega = c.getString(c.getColumnIndexOrThrow("direccion_entrega")),
            estado = EstadoPedido.valueOf(c.getString(c.getColumnIndexOrThrow("estado"))),
            fecha = c.getString(c.getColumnIndexOrThrow("fecha")),
            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
        )
    }
}
