# Mossaq Data Population Script (Windows PowerShell)
# Direct DB insertion to bypass Auth/CSRF issues

$AppUrl = "http://localhost:8080"
$DbName = "mossaq"
$DbUser = "mossaq_user"
$DbPass = "mossaq_password"
$UploadsDir = Join-Path $PSScriptRoot "uploads"

# Colors for Write-Host
$Green = "Green"
$Red = "Red"

# Ensure uploads directory exists
if (-not (Test-Path -Path $UploadsDir)) {
    New-Item -ItemType Directory -Path $UploadsDir | Out-Null
}

# Check DB connection
$ExecMethod = "none"
$DbContainer = ""

if (Get-Command "psql" -ErrorAction SilentlyContinue) {
    $ExecMethod = "local"
} elseif (Get-Command "docker" -ErrorAction SilentlyContinue) {
    # Find container with image name containing "postgres"
    $DbContainer = docker ps --format '{{.ID}} {{.Image}}' | Select-String "postgres" | ForEach-Object { $_.ToString().Split(' ')[0] } | Select-Object -First 1
    if ($DbContainer) {
        $ExecMethod = "docker"
    }
}

if ($ExecMethod -eq "none") {
    Write-Host "Error: psql or Docker not found. Cannot populate DB." -ForegroundColor $Red
    exit 1
}

function Run-Sql {
    param([string]$Sql)
    if ($ExecMethod -eq "local") {
        $env:PGPASSWORD = $DbPass
        psql -h localhost -U $DbUser -d $DbName -c "$Sql" | Out-Null
        $env:PGPASSWORD = $null
    } else {
        docker exec -i $DbContainer psql -U $DbUser -d $DbName -c "$Sql" | Out-Null
    }
}

function Get-SqlResult {
    param([string]$Sql)
    if ($ExecMethod -eq "local") {
        $env:PGPASSWORD = $DbPass
        # -t for tuples only (no headers), -A for unaligned (less whitespace)
        $res = psql -h localhost -U $DbUser -d $DbName -t -A -c "$Sql"
        $env:PGPASSWORD = $null
        return $res
    } else {
        return docker exec -i $DbContainer psql -U $DbUser -d $DbName -t -A -c "$Sql"
    }
}

# Ensure Alice exists for ownership
Run-Sql "CREATE EXTENSION IF NOT EXISTS `"pgcrypto`";"
Run-Sql "ALTER TABLE users ADD COLUMN IF NOT EXISTS email_notifications BOOLEAN NOT NULL DEFAULT TRUE;"

# Password hash contains $ which must be escaped in double-quoted strings or use single quotes
$AlicePass = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
Run-Sql "INSERT INTO users (uuid, email, password, username, role, email_notifications) VALUES (gen_random_uuid(), 'alice@mossaq.com', '$AlicePass', 'Alice', 'USER', true) ON CONFLICT (email) DO NOTHING;"

# Get Alice's UUID
$UserId = (Get-SqlResult "SELECT uuid FROM users WHERE email='alice@mossaq.com';").Trim()

Write-Host "Downloading sample cover art..."
try {
    Invoke-WebRequest -Uri "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=200&auto=format&fit=crop" -OutFile "cover.jpg"
} catch {
    Write-Host "Warning: Could not download cover art." -ForegroundColor Yellow
}

function Upload-Track {
    param($Title, $Artist, $Url)
    
    $Filename = $Url.Split('/')[-1]

    Write-Host "--------------------------------------------------"
    Write-Host "Processing: $Title - $Artist"
    Write-Host "Downloading $Filename..."
    
    try {
        Invoke-WebRequest -Uri $Url -OutFile $Filename
    } catch {
        Write-Host "Failed to download file." -ForegroundColor $Red
        return
    }

    if (-not (Test-Path $Filename)) {
        Write-Host "Failed to download file." -ForegroundColor $Red
        return
    }

    # Prepare files
    $Timestamp = [int][double]::Parse((Get-Date -UFormat %s))
    $StoredAudio = "${Timestamp}_${Filename}"
    $StoredImage = "${Timestamp}_cover.jpg"
    
    Move-Item -Path $Filename -Destination (Join-Path $UploadsDir $StoredAudio) -Force
    if (Test-Path "cover.jpg") {
        Copy-Item -Path "cover.jpg" -Destination (Join-Path $UploadsDir $StoredImage) -Force
    }
    
    $AbsAudioPath = (Join-Path $UploadsDir $StoredAudio).Replace("\", "\\")
    
    # Insert DB Record
    Run-Sql "INSERT INTO tracks (id, title, artist, user_id, filename, content_type, audio_file_path, image_file_path, play_count, like_count) VALUES (gen_random_uuid(), '$Title', '$Artist', '$UserId', '$StoredAudio', 'audio/mpeg', '$AbsAudioPath', '$StoredImage', 0, 0);"
    
    Write-Host "Success! Added to DB and Uploads." -ForegroundColor $Green
}

# Sample Data
Upload-Track "Synthwave Dreams" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
Upload-Track "Neon Nights" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
Upload-Track "Cyber Pulse" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
Upload-Track "Retro Future" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"

Remove-Item "cover.jpg" -ErrorAction SilentlyContinue
Write-Host "--------------------------------------------------"
Write-Host "Population complete! Visit $AppUrl/main to see the tracks." -ForegroundColor $Green