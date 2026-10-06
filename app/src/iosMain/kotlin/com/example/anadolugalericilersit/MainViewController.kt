package com.example.anadolugalericilersit

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
import com.example.anadolugalericilersit.navigation.AnadoluNavGraph
import com.example.anadolugalericilersit.ui.theme.AnadoluGalericilerSitTheme
import platform.UIKit.UIViewController

private class IosViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    val viewModelStoreOwner = remember { IosViewModelStoreOwner() }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner
    ) {
        AnadoluGalericilerSitTheme {
            val navController = rememberNavController()
            AnadoluNavGraph(navController = navController)
        }
    }
}
