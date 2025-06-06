package com.farhanfad0036.asses3olshop.ui.theme.screen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farhanfad0036.asses3olshop.model.Barang
import com.farhanfad0036.asses3olshop.network.BarangApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    var data = mutableStateOf(emptyList<Barang>())
        private set

    init {
        retrieveData()
    }
    private fun retrieveData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                data.value = BarangApi.service.getBarang()
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
            }
        }
    }
}