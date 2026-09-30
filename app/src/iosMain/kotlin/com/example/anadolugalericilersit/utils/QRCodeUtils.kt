package com.example.anadolugalericilersit.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.example.anadolugalericilersit.data.model.Dealer

actual object QRCodeUtils {
    actual fun generateQrCodeBitmap(content: String, sizePx: Int): ImageBitmap? = null
    actual fun sendIbanToWhatsApp(context: Any?, dealer: Dealer) {}
}

@Composable
actual fun CameraQrScannerDialog(
    onQrCodeScanned: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    onDismissRequest()
}
