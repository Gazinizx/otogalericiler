$projectId = "galeri-6fa72"
$baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

$file1 = "app/src/main/res/drawable/6 Damga, Büyük Hediye Fırsatı.png"
$file2 = "app/src/main/res/drawable/DPF Temizliği ve Hediye ECU Kontrolü.png"

$bytes1 = [System.IO.File]::ReadAllBytes($file1)
$b64_1 = [Convert]::ToBase64String($bytes1)
$imageUrl1 = "data:image/png;base64,$b64_1"

$bytes2 = [System.IO.File]::ReadAllBytes($file2)
$b64_2 = [Convert]::ToBase64String($bytes2)
$imageUrl2 = "data:image/png;base64,$b64_2"

$id1 = "campaign_1"
$body1 = @{
    fields = @{
        id = @{ stringValue = $id1 }
        userId = @{ stringValue = "all" }
        title = @{ stringValue = "1" }
        message = @{ stringValue = "6 Damga, Büyük Hediye Fırsatı" }
        imageUrl = @{ stringValue = $imageUrl1 }
        type = @{ stringValue = "ANNOUNCEMENT" }
        createdAt = @{ integerValue = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() }
        isRead = @{ booleanValue = $false }
    }
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "$baseUrl/notifications/$id1" -Method Patch -Body $body1 -ContentType "application/json"

$id2 = "campaign_2"
$body2 = @{
    fields = @{
        id = @{ stringValue = $id2 }
        userId = @{ stringValue = "all" }
        title = @{ stringValue = "2" }
        message = @{ stringValue = "DPF Temizliği ve Hediye ECU Kontrolü" }
        imageUrl = @{ stringValue = $imageUrl2 }
        type = @{ stringValue = "ANNOUNCEMENT" }
        createdAt = @{ integerValue = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() }
        isRead = @{ booleanValue = $false }
    }
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "$baseUrl/notifications/$id2" -Method Patch -Body $body2 -ContentType "application/json"

Write-Host "Campaigns 1 and 2 seeded successfully!"
