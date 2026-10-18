package cl.oylfrutossecos.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oylfrutossecos.app.data.repository.OylRepository
import cl.oylfrutossecos.app.domain.model.CampoFormulario
import cl.oylfrutossecos.app.domain.model.Rol
import cl.oylfrutossecos.app.domain.model.Usuario
import cl.oylfrutossecos.app.utils.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val rolSeleccionado: Rol = Rol.CLIENTE,
    val aliasOCorreo: CampoFormulario = CampoFormulario(),
    val contrasena: CampoFormulario = CampoFormulario(),
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val loginExitoso: Boolean = false
) {
    val formularioValido: Boolean
        get() = aliasOCorreo.valor.isNotBlank() && contrasena.valor.isNotBlank() &&
                aliasOCorreo.esValido && contrasena.esValido
}

data class RegistroUiState(
    val nombre: CampoFormulario = CampoFormulario(),
    val alias: CampoFormulario = CampoFormulario(),
    val correo: CampoFormulario = CampoFormulario(),
    val telefono: CampoFormulario = CampoFormulario(),
    val direccion: CampoFormulario = CampoFormulario(),
    val contrasena: CampoFormulario = CampoFormulario(),
    val confirmarContrasena: CampoFormulario = CampoFormulario(),
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val registroExitoso: Boolean = false
) {
    val formularioValido: Boolean
        get() = nombre.valor.isNotBlank() && alias.valor.isNotBlank() &&
                correo.valor.isNotBlank() && telefono.valor.isNotBlank() &&
                direccion.valor.isNotBlank() && contrasena.valor.isNotBlank() &&
                nombre.esValido && alias.esValido && correo.esValido &&
                telefono.esValido && direccion.esValido && contrasena.esValido &&
                confirmarContrasena.esValido
}

class AuthViewModel(private val repository: OylRepository) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registroState = MutableStateFlow(RegistroUiState())
    val registroState: StateFlow<RegistroUiState> = _registroState.asStateFlow()

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    init {
        // Restaurar sesión previa guardada en SQLite si existe
        verificarSesionPrevia()
    }

    fun verificarSesionPrevia() {
        viewModelScope.launch {
            val guardado = repository.obtenerUsuarioActivo()
            _usuarioActivo.value = guardado
        }
    }

    // LOGIN INTENTS

    fun cambiarRolLogin(rol: Rol) {
        _loginState.update { it.copy(rolSeleccionado = rol, errorGeneral = null) }
        // Si selecciona Admin, sugerir las credenciales preconfiguradas
        if (rol == Rol.ADMINISTRADOR) {
            _loginState.update {
                it.copy(
                    aliasOCorreo = CampoFormulario(valor = "admin"),
                    contrasena = CampoFormulario(valor = "admin")
                )
            }
        } else {
            _loginState.update {
                it.copy(
                    aliasOCorreo = CampoFormulario(valor = "ana"),
                    contrasena = CampoFormulario(valor = "1234")
                )
            }
        }
    }

    fun cambiarAliasLogin(valor: String) {
        _loginState.update {
            it.copy(
                aliasOCorreo = CampoFormulario(valor, Validadores.validarTextoRequerido(valor, "Usuario")),
                errorGeneral = null
            )
        }
    }

    fun cambiarContrasenaLogin(valor: String) {
        _loginState.update {
            it.copy(
                contrasena = CampoFormulario(valor, Validadores.validarContrasena(valor)),
                errorGeneral = null
            )
        }
    }

    fun iniciarSesion(alCompletar: (Rol) -> Unit) {
        val actual = _loginState.value
        val validado = actual.copy(
            aliasOCorreo = actual.aliasOCorreo.copy(error = Validadores.validarTextoRequerido(actual.aliasOCorreo.valor, "Usuario")),
            contrasena = actual.contrasena.copy(error = Validadores.validarContrasena(actual.contrasena.valor))
        )
        _loginState.value = validado

        if (!validado.formularioValido) {
            _loginState.update { it.copy(errorGeneral = "Completa los campos obligatorios") }
            return
        }

        _loginState.update { it.copy(cargando = true, errorGeneral = null) }

        viewModelScope.launch {
            val usuario = repository.iniciarSesion(
                validado.aliasOCorreo.valor,
                validado.contrasena.valor
            )
            _loginState.update { it.copy(cargando = false) }

            if (usuario != null) {
                _usuarioActivo.value = usuario
                _loginState.update { it.copy(loginExitoso = true) }
                alCompletar(usuario.rol)
            } else {
                _loginState.update {
                    it.copy(errorGeneral = "Credenciales incorrectas. Verifica tu usuario y contraseña.")
                }
            }
        }
    }

    // REGISTRO INTENTS

    fun cambiarNombreRegistro(valor: String) = _registroState.update {
        it.copy(nombre = CampoFormulario(valor, Validadores.validarTextoRequerido(valor, "Nombre")), errorGeneral = null)
    }

    fun cambiarAliasRegistro(valor: String) = _registroState.update {
        it.copy(alias = CampoFormulario(valor, Validadores.validarAlias(valor)), errorGeneral = null)
    }

    fun cambiarCorreoRegistro(valor: String) = _registroState.update {
        it.copy(correo = CampoFormulario(valor, Validadores.validarCorreo(valor)), errorGeneral = null)
    }

    fun cambiarTelefonoRegistro(valor: String) = _registroState.update {
        it.copy(telefono = CampoFormulario(valor, Validadores.validarTelefono(valor)), errorGeneral = null)
    }

    fun cambiarDireccionRegistro(valor: String) = _registroState.update {
        it.copy(direccion = CampoFormulario(valor, Validadores.validarTextoRequerido(valor, "Dirección")), errorGeneral = null)
    }

    fun cambiarContrasenaRegistro(valor: String) = _registroState.update {
        it.copy(contrasena = CampoFormulario(valor, Validadores.validarContrasena(valor)), errorGeneral = null)
    }

    fun cambiarConfirmarContrasenaRegistro(valor: String) = _registroState.update {
        val error = if (valor != it.contrasena.valor) "Las contraseñas no coinciden" else null
        it.copy(confirmarContrasena = CampoFormulario(valor, error), errorGeneral = null)
    }

    fun registrar(alCompletar: () -> Unit) {
        val actual = _registroState.value
        val validado = actual.copy(
            nombre = actual.nombre.copy(error = Validadores.validarTextoRequerido(actual.nombre.valor, "Nombre")),
            alias = actual.alias.copy(error = Validadores.validarAlias(actual.alias.valor)),
            correo = actual.correo.copy(error = Validadores.validarCorreo(actual.correo.valor)),
            telefono = actual.telefono.copy(error = Validadores.validarTelefono(actual.telefono.valor)),
            direccion = actual.direccion.copy(error = Validadores.validarTextoRequerido(actual.direccion.valor, "Dirección")),
            contrasena = actual.contrasena.copy(error = Validadores.validarContrasena(actual.contrasena.valor)),
            confirmarContrasena = actual.confirmarContrasena.copy(
                error = if (actual.confirmarContrasena.valor != actual.contrasena.valor) "Las contraseñas no coinciden" else null
            )
        )
        _registroState.value = validado

        if (!validado.formularioValido) {
            _registroState.update { it.copy(errorGeneral = "Por favor corrige los errores del formulario") }
            return
        }

        _registroState.update { it.copy(cargando = true, errorGeneral = null) }

        viewModelScope.launch {
            val resultado = repository.registrarCliente(
                nombre = validado.nombre.valor,
                alias = validado.alias.valor,
                correo = validado.correo.valor,
                telefono = validado.telefono.valor,
                direccion = validado.direccion.valor,
                contrasena = validado.contrasena.valor
            )
            _registroState.update { it.copy(cargando = false) }

            resultado.onSuccess { usuario ->
                _usuarioActivo.value = usuario
                _registroState.update { it.copy(registroExitoso = true) }
                alCompletar()
            }.onFailure { err ->
                _registroState.update { it.copy(errorGeneral = err.message ?: "Error al registrar cliente") }
            }
        }
    }

    fun cerrarSesion(alCerrar: () -> Unit) {
        viewModelScope.launch {
            repository.cerrarSesion()
            _usuarioActivo.value = null
            _loginState.value = LoginUiState()
            _registroState.value = RegistroUiState()
            alCerrar()
        }
    }

    fun actualizarAvatar(uri: String) {
        val actual = _usuarioActivo.value ?: return
        val actualizado = actual.copy(avatarUri = uri)
        viewModelScope.launch {
            repository.actualizarPerfil(actualizado)
            _usuarioActivo.value = actualizado
        }
    }
}