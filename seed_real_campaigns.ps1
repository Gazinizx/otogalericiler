$apiKey = "6d207e02198a847aa98d0a2a901485a5"
$file1 = "app/src/main/res/drawable/damga_hediye_firsati.png"
$file2 = "app/src/main/res/drawable/dpf_temizligi_ecu.png"

function Upload-ImgBB($path) {
    try {
        $client = New-Object System.Net.Http.HttpClient
        $bytes = [System.IO.File]::ReadAllBytes($path)
        $b64 = [Convert]::ToBase64String($bytes)
        $content = New-Object System.Net.Http.FormUrlEncodedContent(@{
            image = $b64
        })
        $uri = "https://api.imgbb.com/1/upload?key=$apiKey"
        $response = $client.PostAsync($uri, $content).Result
        $json = $response.Content.ReadAsStringAsync().Result
        $obj = $json | ConvertFrom-Json
        return $obj.data.url
    } catch {
        Write-Host "Error uploading image"
        return $null
    }
}

$url1 = Upload-ImgBB $file1
$url2 = Upload-ImgBB $file2

Write-Host "URL 1: $url1"
Write-Host "URL 2: $url2"

$projectId = "galeri-6fa72"
$baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

if ($url1) {
    $id1 = "campaign_1"
    $body1 = @{
        fields = @{
            id = @{ stringValue = $id1 }
            userId = @{ stringValue = "all" }
            title = @{ stringValue = "1" }
            message = @{ stringValue = "6 Damga, Büyük Hediye Fırsatı" }
            imageUrl = @{ stringValue = $url1 }
            type = @{ stringValue = "ANNOUNCEMENT" }
            createdAt = @{ integerValue = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() }
            isRead = @{ booleanValue = $false }
        }
    } | ConvertTo-Json -Depth 10
    Invoke-RestMethod -Uri "$baseUrl/notifications/$id1" -Method Patch -Body $body1 -ContentType "application/json"
}

if ($url2) {
    $id2 = "campaign_2"
    $body2 = @{
        fields = @{
            id = @{ stringValue = $id2 }
            userId = @{ stringValue = "all" }
            title = @{ stringValue = "2" }
            message = @{ stringValue = "DPF Temizliği ve Hediye ECU Kontrolü" }
            imageUrl = @{ stringValue = $url2 }
            type = @{ stringValue = "ANNOUNCEMENT" }
            createdAt = @{ integerValue = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() }
            isRead = @{ booleanValue = $false }
        }
    } | ConvertTo-Json -Depth 10
    Invoke-RestMethod -Uri "$baseUrl/notifications/$id2" -Method Patch -Body $body2 -ContentType "application/json"
}

Write-Host "Real campaigns 1 and 2 uploaded and published successfully!"
