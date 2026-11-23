package cl.oylfrutossecos.app.data.remote.api

import cl.oylfrutossecos.app.data.remote.model.AjusteStockDto
import cl.oylfrutossecos.app.data.remote.model.ProductoDto
import retrofit2.Response
import retrofit2.http.*

interface CatalogoApiService {

    @GET("api/productos")
    suspend fun listarProductos(
        @Query("categoria") categoria: String? = null,
        @Query("buscar") buscar: String? = null
    ): Response<List<ProductoDto>>

    @GET("api/productos/{id}")
    suspend fun buscarPorId(@Path("id") id: Long): Response<ProductoDto>

    @GET("api/productos/qr/{codigoQr}")
    suspend fun buscarPorQr(@Path("codigoQr") codigoQr: String): Response<ProductoDto>

    @GET("api/productos/stock-critico")
    suspend fun listarStockCritico(@Query("limite") limite: Int = 10): Response<List<ProductoDto>>

    @POST("api/productos")
    suspend fun crearProducto(@Body producto: ProductoDto): Response<ProductoDto>

    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: Long, @Body producto: ProductoDto): Response<ProductoDto>

    @PATCH("api/productos/{id}/stock")
    suspend fun ajustarStock(@Path("id") id: Long, @Body payload: AjusteStockDto): Response<ProductoDto>

    @DELETE("api/productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Long): Response<Unit>
}
