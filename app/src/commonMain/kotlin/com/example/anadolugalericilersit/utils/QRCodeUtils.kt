package com.example.anadolugalericilersit.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.example.anadolugalericilersit.data.model.Dealer

expect object QRCodeUtils {
    fun generateQrCodeBitmap(content: String, sizePx: Int = 512): ImageBitmap?
    fun sendIbanToWhatsApp(context: Any? = null, dealer: Dealer)
}

@Composable
expect fun CameraQrScannerDialog(
    onQrCodeScanned: (String) -> Unit,
    onDismissRequest: () -> Unit
)
