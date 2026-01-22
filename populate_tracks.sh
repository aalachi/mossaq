#!/bin/bash

# Mossaq Data Population Script
# Direct DB insertion to bypass Auth/CSRF issues

APP_URL="http://localhost:8080"
DB_NAME="mossaq"
DB_USER="mossaq_user"
DB_PASS="mossaq_password"
UPLOADS_DIR="$(pwd)/uploads"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Ensure uploads directory exists
mkdir -p "$UPLOADS_DIR"

# Check DB connection
EXEC_METHOD="none"
DB_CONTAINER=""

if command -v psql &> /dev/null; then
    EXEC_METHOD="local"
elif command -v docker &> /dev/null; then
    DB_CONTAINER=$(docker ps --format '{{.ID}} {{.Image}}' | grep -i "postgres" | awk '{print $1}' | head -n 1)
    if [ -n "$DB_CONTAINER" ]; then
        EXEC_METHOD="docker"
    fi
fi

if [ "$EXEC_METHOD" == "none" ]; then
    echo -e "${RED}Error: psql or Docker not found. Cannot populate DB.${NC}"
    exit 1
fi

run_sql() {
    local sql="$1"
    if [ "$EXEC_METHOD" == "local" ]; then
        export PGPASSWORD=$DB_PASS
        psql -h localhost -U $DB_USER -d $DB_NAME -c "$sql" > /dev/null
    else
        docker exec -i "$DB_CONTAINER" psql -U $DB_USER -d $DB_NAME -c "$sql" > /dev/null
    fi
}

# Ensure Alice exists for ownership
run_sql "CREATE EXTENSION IF NOT EXISTS \"pgcrypto\";"
run_sql "INSERT INTO users (uuid, email, password, username, role) VALUES (gen_random_uuid(), 'alice@mossaq.com', '\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Alice', 'USER') ON CONFLICT (email) DO NOTHING;"

# Get Alice's UUID
if [ "$EXEC_METHOD" == "local" ]; then
    export PGPASSWORD=$DB_PASS
    USER_ID=$(psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT uuid FROM users WHERE email='alice@mossaq.com';" | xargs)
else
    USER_ID=$(docker exec -i "$DB_CONTAINER" psql -U $DB_USER -d $DB_NAME -t -c "SELECT uuid FROM users WHERE email='alice@mossaq.com';" | tr -d '\r' | xargs)
fi

echo "Downloading sample cover art..."
curl -L -o "cover.jpg" "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=200&auto=format&fit=crop" --silent

upload_track() {
    local title="$1"
    local artist="$2"
    local url="$3"
    local filename=$(basename "$url")

    echo "--------------------------------------------------"
    echo "Processing: $title - $artist"
    echo "Downloading $filename..."
    curl -L -o "$filename" "$url" --silent

    if [ ! -f "$filename" ]; then
        echo -e "${RED}Failed to download file.${NC}"
        return
    fi

    # Prepare files
    local timestamp=$(date +%s)
    local stored_audio="${timestamp}_${filename}"
    local stored_image="${timestamp}_cover.jpg"
    
    mv "$filename" "$UPLOADS_DIR/$stored_audio"
    cp "cover.jpg" "$UPLOADS_DIR/$stored_image"
    
    local abs_audio_path="$UPLOADS_DIR/$stored_audio"
    
    # Insert DB Record
    run_sql "INSERT INTO tracks (id, title, artist, user_id, filename, content_type, audio_file_path, image_file_path, play_count, like_count) VALUES (gen_random_uuid(), '$title', '$artist', '$USER_ID', '$stored_audio', 'audio/mpeg', '$abs_audio_path', '$stored_image', 0, 0);"
    
    echo -e "${GREEN}Success! Added to DB and Uploads.${NC}"
}

# Sample Data
upload_track "Synthwave Dreams" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
upload_track "Neon Nights" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
upload_track "Cyber Pulse" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
upload_track "Retro Future" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"

rm "cover.jpg"
echo "--------------------------------------------------"
echo -e "${GREEN}Population complete! Visit $APP_URL/main to see the tracks.${NC}"