package com.example.anadolugalericilersit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.VehicleStatus

@Composable
fun VehicleStatusBadge(status: VehicleStatus, modifier: Modifier = Modifier) {
    val (label, bgColor, textColor) = when (status) {
        VehicleStatus.PUBLISHED -> Triple("Yayında", Color(0xFFDCFCE7), Color(0xFF15803D))
        VehicleStatus.PENDING -> Triple("Onay Bekliyor", Color(0xFFFEF9C3), Color(0xFFA16207))
        VehicleStatus.DRAFT -> Triple("Taslak", Color(0xFFF3F4F6), Color(0xFF4B5563))
        VehicleStatus.REJECTED -> Triple("Reddedildi", Color(0xFFFEE2E2), Color(0xFFB91C1C))
        VehicleStatus.SOLD -> Triple("Satıldı", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
        VehicleStatus.PASSIVE -> Triple("Pasif", Color(0xFFE5E7EB), Color(0xFF374151))
    }

    Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun DealerStatusBadge(status: DealerStatus, modifier: Modifier = Modifier) {
    val (label, bgColor, textColor) = when (status) {
        DealerStatus.APPROVED -> Triple("Onaylı Galerici", Color(0xFFDCFCE7), Color(0xFF15803D))
        DealerStatus.PENDING -> Triple("Onay Bekliyor", Color(0xFFFEF9C3), Color(0xFFA16207))
        DealerStatus.REJECTED -> Triple("Reddedildi", Color(0xFFFEE2E2), Color(0xFFB91C1C))
        DealerStatus.SUSPENDED -> Triple("Askıda", Color(0xFFFED7AA), Color(0xFFC2410C))
    }

    Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
