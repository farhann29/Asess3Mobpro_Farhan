package com.farhanfad0036.asses3olshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.farhanfad0036.asses3olshop.ui.theme.screen.MainScreen
import com.farhanfad0036.asses3olshop.ui.theme.theme.Asses3OlshopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Asses3OlshopTheme {
                MainScreen()
            }
        }
    }
}