package com.farhanfad0036.asses3olshop.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

private const val BASE_URL = ""

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface ShopApiService {
    @GET("static-api.jason")
    suspend fun getBarang(): String
}

object BarangApi {
    val service: ShopApiService by lazy {
        retrofit.create(ShopApiService::class.java)
    }
}