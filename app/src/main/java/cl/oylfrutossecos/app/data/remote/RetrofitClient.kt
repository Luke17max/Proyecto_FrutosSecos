package cl.oylfrutossecos.app.data.remote

import cl.oylfrutossecos.app.data.remote.api.ApiExternaService
import cl.oylfrutossecos.app.data.remote.api.CatalogoApiService
import cl.oylfrutossecos.app.data.remote.api.PedidosApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 es la direccion para comunicarse con localhost desde el emulador de Android
    private const val BASE_URL_CATALOGO = "http://10.0.2.2:8081/"
    private const val BASE_URL_PEDIDOS = "http://10.0.2.2:8082/"
    private const val BASE_URL_EXTERNA = "https://jsonplaceholder.typicode.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    val catalogoApi: CatalogoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CATALOGO)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatalogoApiService::class.java)
    }

    val pedidosApi: PedidosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_PEDIDOS)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PedidosApiService::class.java)
    }

    val apiExterna: ApiExternaService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_EXTERNA)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiExternaService::class.java)
    }
}
