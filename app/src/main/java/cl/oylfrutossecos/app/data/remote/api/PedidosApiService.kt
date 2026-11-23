package cl.oylfrutossecos.app.data.remote.api

import cl.oylfrutossecos.app.data.remote.model.EstadoPedidoDto
import cl.oylfrutossecos.app.data.remote.model.PedidoDto
import cl.oylfrutossecos.app.data.remote.model.UsuarioDto
import retrofit2.Response
import retrofit2.http.*

interface PedidosApiService {

    @GET("api/pedidos")
    suspend fun listarPedidos(
        @Query("clienteId") clienteId: Long? = null,
        @Query("estado") estado: String? = null
    ): Response<List<PedidoDto>>

    @GET("api/pedidos/{id}")
    suspend fun buscarPorId(@Path("id") id: Long): Response<PedidoDto>

    @GET("api/pedidos/numero/{numeroPedido}")
    suspend fun buscarPorNumero(@Path("numeroPedido") numeroPedido: String): Response<PedidoDto>

    @POST("api/pedidos")
    suspend fun crearPedido(@Body pedido: PedidoDto): Response<PedidoDto>

    @PATCH("api/pedidos/{id}/estado")
    suspend fun actualizarEstado(
        @Path("id") id: Long,
        @Body payload: EstadoPedidoDto
    ): Response<PedidoDto>

    @POST("api/pedidos/usuarios/login")
    suspend fun login(@Body credenciales: Map<String, String>): Response<UsuarioDto>

    @POST("api/pedidos/usuarios/registro")
    suspend fun registrar(@Body usuario: UsuarioDto): Response<UsuarioDto>

    @GET("api/pedidos/usuarios")
    suspend fun listarUsuarios(): Response<List<UsuarioDto>>

    @GET("api/pedidos/usuarios/{id}")
    suspend fun buscarUsuarioPorId(@Path("id") id: Long): Response<UsuarioDto>
}
