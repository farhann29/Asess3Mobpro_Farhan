package com.farhanfad0036.asses3olshop.network

import com.farhanfad0036.asses3olshop.model.Barang
import com.farhanfad0036.asses3olshop.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

private const val BASE_URL = ""

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface BarangApiService {
    @GET("barang.php")
    suspend fun getBarang(
        @Header("Authorization") userId: String
    ): List<Barang>

    @Multipart
    @POST("barang.php")
    suspend fun postBarang(
        @Header("Authorization") userId: String,
        @Part("nama") nama: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("noWa") noWa: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus
}

object BarangApi {
    val service: BarangApiService by lazy {
        retrofit.create(BarangApiService::class.java)
    }

    fun getBarangUrl(imageId: String): String {
        return "${BASE_URL}image.php?id=$imageId"
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED}