package cl.oylfrutossecos.app.data.remote.api

import cl.oylfrutossecos.app.data.remote.model.PostExternoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiExternaService {

    @GET("posts")
    suspend fun obtenerPublicaciones(
        @Query("_limit") limite: Int = 5
    ): Response<List<PostExternoDto>>
}
