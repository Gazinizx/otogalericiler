package com.example.anadolugalericilersit.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordDialog(
    initialEmail: String = "",
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Şifremi Unuttum") },
        text = {
            Column {
                Text(text = "Kayıtlı e-posta adresinizi giriniz. Şifre sıfırlama bağlantısı Gmail / E-posta kutunuza gönderilecektir.")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta Adresi") },
                    placeholder = { Text("örnek@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(email) },
                enabled = email.isNotBlank()
            ) {
                Text(text = "Sıfırlama Linki Gönder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal")
            }
        }
    )
}
