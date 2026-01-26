#!/bin/bash

# Mossaq Local Uploads Population Script
# Scans the ./uploads directory and inserts DB records for any MP3s found.
# This avoids downloading files or duplicating them via the API.

DB_NAME="mossaq"
DB_USER="mossaq_user"
DB_PASS="mossaq_password"
UPLOADS_DIR="$(pwd)/uploads"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Check if uploads dir exists
if [ ! -d "$UPLOADS_DIR" ]; then
    echo -e "${RED}Error: $UPLOADS_DIR does not exist.${NC}"
    exit 1
fi

# DB Connection Logic (Docker/Local)
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
    echo -e "${RED}Error: psql or Docker not found.${NC}"
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

echo "Ensuring database is ready..."
run_sql "CREATE EXTENSION IF NOT EXISTS \"pgcrypto\";"
# Ensure Alice exists to own the tracks
run_sql "INSERT INTO users (uuid, email, password, username, role, email_notifications) VALUES (gen_random_uuid(), 'alice@mossaq.com', '\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Alice', 'USER', true) ON CONFLICT (email) DO NOTHING;"

# Get Alice's UUID
if [ "$EXEC_METHOD" == "local" ]; then
    export PGPASSWORD=$DB_PASS
    USER_ID=$(psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT uuid FROM users WHERE email='alice@mossaq.com';" | xargs)
else
    USER_ID=$(docker exec -i "$DB_CONTAINER" psql -U $DB_USER -d $DB_NAME -t -c "SELECT uuid FROM users WHERE email='alice@mossaq.com';" | tr -d '\r' | xargs)
fi

echo "Scanning $UPLOADS_DIR for tracks..."

count=0
# Loop through mp3 files (case insensitive)
find "$UPLOADS_DIR" -maxdepth 1 -iname "*.mp3" -print0 | while IFS= read -r -d '' file; do
    filename=$(basename "$file")
    
    # Escape filename for SQL safety
    safe_filename=$(echo "$filename" | sed "s/'/''/g")
    
    # Check if already exists in DB to avoid duplicates
    exists_query="SELECT count(*) FROM tracks WHERE filename = '$safe_filename';"
    if [ "$EXEC_METHOD" == "local" ]; then
        exists=$(psql -h localhost -U $DB_USER -d $DB_NAME -t -c "$exists_query" | xargs)
    else
        exists=$(docker exec -i "$DB_CONTAINER" psql -U $DB_USER -d $DB_NAME -t -c "$exists_query" | tr -d '\r' | xargs)
    fi
    
    if [ "$exists" -gt "0" ]; then
        echo "Skipping $filename (already in DB)"
        continue
    fi

    echo "Adding $filename..."
    
    # Title: remove extension, replace underscores/hyphens with spaces
    title="${filename%.*}"
    title=$(echo "$title" | sed 's/[_-]/ /g')
    safe_title=$(echo "$title" | sed "s/'/''/g")
    
    # Image: check for jpg/png with same base name
    base="${file%.*}"
    image_sql="NULL"
    
    if [ -f "${base}.jpg" ]; then image_sql="'"$(basename "${base}.jpg")"'"; fi
    if [ -f "${base}.png" ]; then image_sql="'"$(basename "${base}.png")"'"; fi
    if [ -f "${base}.jpeg" ]; then image_sql="'"$(basename "${base}.jpeg")"'"; fi
    
    # Insert into DB
    run_sql "INSERT INTO tracks (id, title, artist, user_id, filename, content_type, audio_file_path, image_file_path, play_count, like_count, uploaded_at) VALUES (gen_random_uuid(), '$safe_title', 'Local Upload', '$USER_ID', '$safe_filename', 'audio/mpeg', '$file', $image_sql, 0, 0, $(date +%s)000);"
    
    ((count++))
done

echo -e "${GREEN}Done! Added $count new tracks from local uploads.${NC}"