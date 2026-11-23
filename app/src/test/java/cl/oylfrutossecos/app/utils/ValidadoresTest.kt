package cl.oylfrutossecos.app.utils

import org.junit.Assert.*
import org.junit.Test

class ValidadoresTest {

    @Test
    fun validarTextoRequerido_cuandoEstaVacio_retornaMensajeError() {
        val resultado = Validadores.validarTextoRequerido("", "Nombre")
        assertNotNull(resultado)
        assertTrue(resultado!!.contains("obligatorio"))
    }

    @Test
    fun validarTextoRequerido_cuandoTieneMenosDeMinimo_retornaMensajeError() {
        val resultado = Validadores.validarTextoRequerido("ab", "Nombre", minimo = 3)
        assertNotNull(resultado)
        assertTrue(resultado!!.contains("al menos 3 caracteres"))
    }

    @Test
    fun validarTextoRequerido_cuandoEsValido_retornaNull() {
        val resultado = Validadores.validarTextoRequerido("Almendras", "Nombre")
        assertNull(resultado)
    }

    @Test
    fun validarCorreo_cuandoFormatoEsInvalido_retornaError() {
        val resultadoInvalido1 = Validadores.validarCorreo("correo_invalido")
        val resultadoInvalido2 = Validadores.validarCorreo("correo@sin_dominio")

        assertNotNull(resultadoInvalido1)
        assertNotNull(resultadoInvalido2)
    }

    @Test
    fun validarCorreo_cuandoFormatoEsValido_retornaNull() {
        val resultado1 = Validadores.validarCorreo("ana@correo.cl")
        val resultado2 = Validadores.validarCorreo("admin@admin.cl")

        assertNull(resultado1)
        assertNull(resultado2)
    }

    @Test
    fun validarContrasena_cuandoEsCorta_retornaError() {
        val resultado = Validadores.validarContrasena("12")
        assertNotNull(resultado)
        assertTrue(resultado!!.contains("al menos 4 caracteres"))
    }

    @Test
    fun validarContrasena_cuandoEsValida_retornaNull() {
        val resultado = Validadores.validarContrasena("admin1234")
        assertNull(resultado)
    }

    @Test
    fun validarAlias_cuandoTieneEspacios_retornaError() {
        val resultado = Validadores.validarAlias("ana torres")
        assertNotNull(resultado)
        assertTrue(resultado!!.contains("no puede contener espacios"))
    }

    @Test
    fun validarAlias_cuandoEsValido_retornaNull() {
        val resultado = Validadores.validarAlias("anatorres")
        assertNull(resultado)
    }

    @Test
    fun validarTelefono_cuandoFormatoChilenoEsValido_retornaNull() {
        val resultado = Validadores.validarTelefono("+56 9 1234 5678")
        assertNull(resultado)
    }
}
