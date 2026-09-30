package com.example.anadolugalericilersit

import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import com.example.anadolugalericilersit.navigation.AnadoluNavGraph
import com.example.anadolugalericilersit.ui.theme.AnadoluGalericilerSitTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    AnadoluGalericilerSitTheme {
        val navController = rememberNavController()
        AnadoluNavGraph(navController = navController)
    }
}
