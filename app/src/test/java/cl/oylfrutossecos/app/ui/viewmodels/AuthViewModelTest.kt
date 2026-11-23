package cl.oylfrutossecos.app.ui.viewmodels

import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.domain.model.Usuario
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: OylRepository = mockk(relaxed = true)
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cambiarRolLogin_actualizaRolEnEstado() {
        viewModel.cambiarRolLogin(Rol.ADMINISTRADOR)
        assertEquals(Rol.ADMINISTRADOR, viewModel.loginState.value.rolSeleccionado)

        viewModel.cambiarRolLogin(Rol.CLIENTE)
        assertEquals(Rol.CLIENTE, viewModel.loginState.value.rolSeleccionado)
    }

    @Test
    fun cambiarAliasLogin_actualizaValorYValida() {
        viewModel.cambiarAliasLogin("ana")
        assertEquals("ana", viewModel.loginState.value.aliasOCorreo.valor)
        assertNull(viewModel.loginState.value.aliasOCorreo.error)

        viewModel.cambiarAliasLogin("")
        assertNotNull(viewModel.loginState.value.aliasOCorreo.error)
    }

    @Test
    fun login_cuandoCredencialesSonCorrectas_actualizaLoginExitoso() = runTest {
        val usuarioSimulado = Usuario(
            id = 2L,
            nombre = "Ana Torres",
            alias = "ana",
            correo = "ana@correo.cl",
            telefono = "+56 9 1234 5678",
            direccion = "Providencia",
            contrasena = "1234",
            rol = Rol.CLIENTE
        )
        every { repository.iniciarSesion("ana", "1234") } returns usuarioSimulado

        viewModel.cambiarAliasLogin("ana")
        viewModel.cambiarContrasenaLogin("1234")
        
        var completadoConRol: Rol? = null
        viewModel.iniciarSesion { rol -> completadoConRol = rol }

        advanceUntilIdle()

        assertTrue(viewModel.loginState.value.loginExitoso)
        assertEquals(Rol.CLIENTE, completadoConRol)
        assertNull(viewModel.loginState.value.errorGeneral)
    }

    @Test
    fun login_cuandoCredencialesSonIncorrectas_estableceErrorGeneral() = runTest {
        every { repository.iniciarSesion("ana", "error") } returns null

        viewModel.cambiarAliasLogin("ana")
        viewModel.cambiarContrasenaLogin("error")
        
        var completado = false
        viewModel.iniciarSesion { completado = true }

        advanceUntilIdle()

        assertFalse(viewModel.loginState.value.loginExitoso)
        assertFalse(completado)
        assertNotNull(viewModel.loginState.value.errorGeneral)
    }
}
