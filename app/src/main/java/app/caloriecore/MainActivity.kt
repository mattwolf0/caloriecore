package app.caloriecore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.caloriecore.ui.CalorieCoreApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCalorieCore()
    }

    private fun showCalorieCore() {
        enableEdgeToEdge()
        setContent {
            CalorieCoreApp()
        }
    }
}
