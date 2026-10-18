package cl.oylfrutossecos.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.oylfrutossecos.app.domain.model.*

class OylDbHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    NOMBRE_BASE_DATOS,
    null,
    VERSION_BASE_DATOS
) {

    companion object {
        const val NOMBRE_BASE_DATOS = "oyl_frutos_secos.db"
        const val VERSION_BASE_DATOS = 1

        // Singleton para evitar múltiples instancias concurrentes
        @Volatile
        private var INSTANCIA: OylDbHelper? = null

        fun obtenerInstancia(context: Context): OylDbHelper {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: OylDbHelper(context).also { INSTANCIA = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Tabla de Usuarios
        db.execSQL(
            """
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                alias TEXT NOT NULL UNIQUE,
                correo TEXT NOT NULL,
                telefono TEXT NOT NULL,
                direccion TEXT NOT NULL,
                contrasena TEXT NOT NULL,
                rol TEXT NOT NULL,
                avatar_uri TEXT
            )
            """.trimIndent()
        )

        // 2. Tabla de Productos (Catálogo)
        db.execSQL(
            """
            CREATE TABLE productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo_qr TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL,
                categoria TEXT NOT NULL,
                precio INTEGER NOT NULL,
                stock INTEGER NOT NULL,
                formato TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                imagen_uri TEXT,
                activo INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )

        // 3. Tabla de Carrito de Compras (Aislado por usuario)
        db.execSQL(
            """
            CREATE TABLE carrito (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            )
            """.trimIndent()
        )

        // 4. Tabla de Pedidos Consolidados
        db.execSQL(
            """
            CREATE TABLE pedidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_pedido TEXT NOT NULL UNIQUE,
                usuario_id INTEGER NOT NULL,
                cliente_nombre TEXT NOT NULL,
                total INTEGER NOT NULL,
                costo_despacho INTEGER NOT NULL DEFAULT 0,
                tipo_entrega TEXT NOT NULL,
                direccion_entrega TEXT,
                estado TEXT NOT NULL,
                fecha TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )
            """.trimIndent()
        )

        // 5. Tabla de Ítems del Pedido
        db.execSQL(
            """
            CREATE TABLE pedido_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pedido_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                nombre_producto TEXT NOT NULL,
                precio_unitario INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                subtotal INTEGER NOT NULL,
                FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 6. Tabla de Notificaciones
        db.execSQL(
            """
            CREATE TABLE notificaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER,
                titulo TEXT NOT NULL,
                mensaje TEXT NOT NULL,
                fecha TEXT NOT NULL,
                leida INTEGER NOT NULL DEFAULT 0,
                destino_ruta TEXT
            )
            """.trimIndent()
        )

        // 7. Tabla de Sesión Activa (guarda el id del usuario autenticado)
        db.execSQL(
            """
            CREATE TABLE sesion_activa (
                id INTEGER PRIMARY KEY,
                usuario_id INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Carga de datos semilla (Mock inicial autorizado en el caso)
        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS sesion_activa")
        db.execSQL("DROP TABLE IF EXISTS notificaciones")
        db.execSQL("DROP TABLE IF EXISTS pedido_items")
        db.execSQL("DROP TABLE IF EXISTS pedidos")
        db.execSQL("DROP TABLE IF EXISTS carrito")
        db.execSQL("DROP TABLE IF EXISTS productos")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        val adminValues = ContentValues().apply {
            put("nombre", "Administrador")
            put("alias", "admin")
            put("correo", "admin@admin.cl")
            put("telefono", "+56 9 1234 1234")
            put("direccion", "Santiago")
            put("contrasena", "admin")
            put("rol", Rol.ADMINISTRADOR.name)
        }
        db.insert("usuarios", null, adminValues)

        // 2. Cliente de prueba inicial
        val clienteValues = ContentValues().apply {
            put("nombre", "Ana Torres")
            put("alias", "ana")
            put("correo", "ana@correo.cl")
            put("telefono", "+56 9 1234 5678")
            put("direccion", "Santiago, RM")
            put("contrasena", "1234")
            put("rol", Rol.CLIENTE.name)
        }
        val anaId = db.insert("usuarios", null, clienteValues)

        // 3. Catálogo sintético oficial OYL Frutos Secos
        val productosSemilla = listOf(
            Producto(
                codigoQr = "OYL-ALM-001",
                nombre = "Almendras Naturales",
                categoria = Categoria.FRUTOS_SECOS,
                precio = 2990,
                stock = 25,
                formato = "250 gr",
                descripcion = "Almendras naturales sin sal de primera calidad. Ricas en proteínas, fibra y grasas saludables."
            ),
            Producto(
                codigoQr = "OYL-NUC-002",
                nombre = "Nueces Mariposa",
                categoria = Categoria.FRUTOS_SECOS,
                precio = 3490,
                stock = 18,
                formato = "250 gr",
                descripcion = "Nueces mariposa seleccionadas, crocantes y ricas en ácidos grasos Omega-3."
            ),
            Producto(
                codigoQr = "OYL-MIX-003",
                nombre = "Mix Premium",
                categoria = Categoria.FRUTOS_SECOS,
                precio = 3990,
                stock = 40,
                formato = "250 gr",
                descripcion = "Combinación equilibrada de almendras, nueces, castañas de cajú y pasas seleccionadas."
            ),
            Producto(
                codigoQr = "OYL-CHI-004",
                nombre = "Semillas de Chía",
                categoria = Categoria.SEMILLAS,
                precio = 2290,
                stock = 20,
                formato = "250 gr",
                descripcion = "Semillas de chía 100% naturales, excelente fuente de fibra soluble y antioxidantes."
            ),
            Producto(
                codigoQr = "OYL-CRA-005",
                nombre = "Cranberries",
                categoria = Categoria.DESHIDRATADOS,
                precio = 2890,
                stock = 15,
                formato = "250 gr",
                descripcion = "Arándanos rojos deshidratados con un toque dulce, ideales para desayunos y repostería."
            ),
            Producto(
                codigoQr = "OYL-PAS-006",
                nombre = "Pasas Corinto",
                categoria = Categoria.DESHIDRATADOS,
                precio = 1890,
                stock = 30,
                formato = "250 gr",
                descripcion = "Pasas morenas seleccionadas de dulzor natural, ricas en hierro y energía rápida."
            ),
            Producto(
                codigoQr = "OYL-CAL-007",
                nombre = "Semillas de Calabaza",
                categoria = Categoria.SEMILLAS,
                precio = 2690,
                stock = 5, // Simulación de stock bajo para alertas de inventario
                formato = "250 gr",
                descripcion = "Semillas de zapallo o calabaza peladas, ricas en magnesio y zinc."
            )
        )

        for (p in productosSemilla) {
            val cv = ContentValues().apply {
                put("codigo_qr", p.codigoQr)
                put("nombre", p.nombre)
                put("categoria", p.categoria.name)
                put("precio", p.precio)
                put("stock", p.stock)
                put("formato", p.formato)
                put("descripcion", p.descripcion)
                put("activo", 1)
            }
            db.insert("productos", null, cv)
        }

        // 4. Notificaciones iniciales de demostración
        val notifAdmin = ContentValues().apply {
            put("usuario_id", null as Long?)
            put("titulo", "Producto con stock bajo")
            put("mensaje", "Semillas de Calabaza tiene 5 unidades disponibles.")
            put("fecha", "Hoy, 10:15")
            put("leida", 0)
            put("destino_ruta", "inventario")
        }
        db.insert("notificaciones", null, notifAdmin)

        val notifCliente = ContentValues().apply {
            put("usuario_id", anaId)
            put("titulo", "Promoción especial")
            put("mensaje", "20% en mix premium durante esta semana.")
            put("fecha", "Ayer, 18:30")
            put("leida", 0)
            put("destino_ruta", "catalogo")
        }
        db.insert("notificaciones", null, notifCliente)
    }
}