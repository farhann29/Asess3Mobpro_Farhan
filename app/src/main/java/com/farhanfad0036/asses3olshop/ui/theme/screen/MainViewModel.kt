package com.farhanfad0036.asses3olshop.ui.theme.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farhanfad0036.asses3olshop.model.Projek
import com.farhanfad0036.asses3olshop.network.ApiStatus
import com.farhanfad0036.asses3olshop.network.ProjekApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream


class MainViewModel : ViewModel() {

    var data = mutableStateOf(emptyList<Projek>())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = ProjekApi.service.getProjek(userId)
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun saveData(userId: String, semester: String, mataKuliah: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = ProjekApi.service.postProjek(
                    userId,
                    semester.toRequestBody("text/plain".toMediaTypeOrNull()),
                    mataKuliah.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )
                if (result.status == "success")
                    retrieveData(userId)
                else
                    throw Exception(result.message)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }
    fun deletedData(userId: String, projekId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = ProjekApi.service.deletedProjek(
                    userId,
                    projekId.toString()
                )
                if (result.status == "success")
                    retrieveData(userId)
                else
                    throw Exception(result.message)
            } catch (e: Exception) {
                Log.d("Mainviewmodel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun updateData(id: String, userId: String, semester: String, mataKuliah: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {

            status.value = ApiStatus.LOADING
            try {
                val result = ProjekApi.service.updateProjek(
                    email = userId,
                    id = id,
                    method = "PUT".toRequestBody("text/plain".toMediaTypeOrNull()),
                    semester = semester.toRequestBody("text/plain".toMediaTypeOrNull()),
                    mataKuliah = mataKuliah.toRequestBody("text/plain".toMediaTypeOrNull()),
                    gambar = bitmap.toMultipartBody()
                )

                if (result.status == "success") {
                    retrieveData(userId)
                } else {
                    throw Exception(result.message)
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Update Failure: ${e.message}")
                errorMessage.value = "Update Gagal: ${e.message}"
                status.value = ApiStatus.FAILED
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size)
        return MultipartBody.Part.createFormData(
            "gambar", "image.jpg", requestBody
        )
    }
    fun clearMessage() {errorMessage.value = null}
}