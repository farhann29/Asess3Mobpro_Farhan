package com.farhanfad0036.asses3olshop.network

import com.farhanfad0036.asses3olshop.model.Barang
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = ""

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface BarangApiService {
    @GET("static-api.jason")
    suspend fun getBarang(): List<Barang>
}

object BarangApi {
    val service: BarangApiService by lazy {
        retrofit.create(BarangApiService::class.java)
    }

    fun getBarangUrl(imageId: String): String {
        return "$BASE_URL$imageId.jpg"
    }
}