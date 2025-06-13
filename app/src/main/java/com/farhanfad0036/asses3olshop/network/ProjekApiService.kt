package com.farhanfad0036.asses3olshop.network

import com.farhanfad0036.asses3olshop.model.Projek
import com.farhanfad0036.asses3olshop.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private const val BASE_URL = "https://score-api.bagasaldianata.my.id/api/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ProjekApiService {
    @GET("scores")
    suspend fun getProjek(
        @Header("Authorization") email: String
    ): List<Projek>

    @Multipart
    @POST("scores")
    suspend fun postProjek(
        @Header("Authorization") email: String,
        @Part("semester") semester: RequestBody,
        @Part("mataKuliah") mataKuliah: RequestBody,
        @Part gambar: MultipartBody.Part
    ): OpStatus

    @DELETE("scores")
    suspend fun deletedProjek(
        @Header("Authorization") email: String,
        @Query("id") id: String
    ): OpStatus

    @Multipart
    @POST("scores")
    suspend fun updateProjek(
        @Header("Authorization") email: String,
        @Query("id") id: String,
        @Part("_method") method: RequestBody,
        @Part("semester") semester: RequestBody,
        @Part("mataKuliah") mataKuliah: RequestBody,
        @Part gambar: MultipartBody.Part
    ): OpStatus
}

object ProjekApi {
    val service: ProjekApiService by lazy {
        retrofit.create(ProjekApiService::class.java)
    }

    fun getProjekUrl(gambar: String): String {
        return "${BASE_URL}image?id=$gambar"
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED}