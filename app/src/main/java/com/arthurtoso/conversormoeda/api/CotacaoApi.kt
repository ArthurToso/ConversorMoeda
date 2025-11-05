package com.arthurtoso.conversormoeda.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CotacaoApi {
    @GET("json/last/{moedas}")
    suspend fun getRates(
        @Path("moedas") moedas: String
    ): Response<Map<String, Cotacao>>
}