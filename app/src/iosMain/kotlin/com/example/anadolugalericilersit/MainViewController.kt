package com.example.anadolugalericilersit

import androidx.compose.ui.window.ComposeUIViewController
import com.example.anadolugalericilersit.navigation.NavGraph
import com.example.anadolugalericilersit.ui.theme.AnadoluTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    AnadoluTheme {
        NavGraph()
    }
}
