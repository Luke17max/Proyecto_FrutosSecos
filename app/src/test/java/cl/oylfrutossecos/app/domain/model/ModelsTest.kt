package cl.oylfrutossecos.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class ModelsTest {

    @Test
    fun producto_tieneStock_retornaTrueCuandoStockMayorQueCero() {
        val prod = Producto(
            id = 1L,
            codigoQr = "OYL-001",
            nombre = "Almendras",
            categoria = Categoria.FRUTOS_SECOS,
            precio = 4500,
            stock = 15,
            formato = "250 gr",
            descripcion = "Tostadas"
        )

        assertTrue(prod.tieneStock)
        assertFalse(prod.stockBajo)
    }

    @Test
    fun producto_stockBajo_retornaTrueCuandoStockEstaEntreUnoYDiez() {
        val prod = Producto(
            id = 2L,
            codigoQr = "OYL-002",
            nombre = "Nueces",
            categoria = Categoria.FRUTOS_SECOS,
            precio = 5200,
            stock = 5,
            formato = "250 gr",
            descripcion = "Extra light"
        )

        assertTrue(prod.tieneStock)
        assertTrue(prod.stockBajo)
    }

    @Test
    fun itemCarrito_subtotal_calculaCorrectamente() {
        val prod = Producto(
            id = 1L,
            codigoQr = "OYL-001",
            nombre = "Almendras",
            categoria = Categoria.FRUTOS_SECOS,
            precio = 4500,
            stock = 15,
            formato = "250 gr",
            descripcion = ""
        )
        val item = ItemCarrito(usuarioId = 1L, producto = prod, cantidad = 3)

        assertEquals(13500, item.subtotal)
    }

    @Test
    fun campoFormulario_esValido_dependeDeErrorNull() {
        val campoValido = CampoFormulario(valor = "Texto", error = null)
        val campoInvalido = CampoFormulario(valor = "", error = "Obligatorio")

        assertTrue(campoValido.esValido)
        assertFalse(campoInvalido.esValido)
    }
}
