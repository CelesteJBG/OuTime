package com.outime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.outime.app.presentation.navigation.AppNavGraph
import com.outime.app.presentation.theme.OuTimeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OuTimeTheme {
                AppNavGraph()
            }
        }
    }
}