#!/bin/bash

# Mossaq Database Population Script
# Creates users, inserts tracks directly to DB/FS, and adds comments.

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

# Execution Helper: Check for psql or Docker
EXEC_METHOD="none"
DB_CONTAINER=""

if command -v psql &> /dev/null; then
    EXEC_METHOD="local"
elif command -v docker &> /dev/null; then
    # Try to find a running container with "postgres" in the image name
    DB_CONTAINER=$(docker ps --format '{{.ID}} {{.Image}}' | grep -i "postgres" | awk '{print $1}' | head -n 1)
    if [ -n "$DB_CONTAINER" ]; then
        EXEC_METHOD="docker"
        echo "psql not found. Using Docker container: $DB_CONTAINER"
    fi
fi

if [ "$EXEC_METHOD" == "none" ]; then
    echo -e "${RED}Error: 'psql' is not installed and no running PostgreSQL Docker container was found.${NC}"
    echo "To fix this:"
    echo "  - macOS: brew install libpq && brew link --force libpq"
    echo "  - Ubuntu: sudo apt-get install postgresql-client"
    echo "  - Or ensure your Docker database container is running."
    exit 1
fi

run_sql() {
    local sql="$1"
    if [ "$EXEC_METHOD" == "local" ]; then
        export PGPASSWORD=$DB_PASS
        psql -h localhost -U $DB_USER -d $DB_NAME -c "$sql" > /dev/null
    else
        # Docker execution
        docker exec -i "$DB_CONTAINER" psql -U $DB_USER -d $DB_NAME -c "$sql" > /dev/null
    fi
}

echo "Checking if Mossaq is running..."
if ! curl --output /dev/null --silent --head --fail "$APP_URL"; then
    echo -e "${RED}Error: Mossaq application is not reachable at $APP_URL${NC}"
    echo "Please start the application first."
    exit 1
fi

# Download a sample cover image to use for uploads
echo "Downloading sample cover art..."
curl -L -o "cover.jpg" "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=200&auto=format&fit=crop" --silent

# 1. Create Users
echo "Creating Users..."
run_sql "
CREATE EXTENSION IF NOT EXISTS \"pgcrypto\";
INSERT INTO users (uuid, email, password, username, role) VALUES 
(gen_random_uuid(), 'alice@mossaq.com', '\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Alice', 'USER'),
(gen_random_uuid(), 'bob@mossaq.com', '\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Bob', 'USER'),
(gen_random_uuid(), 'charlie@mossaq.com', '\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Charlie', 'USER')
ON CONFLICT (email) DO NOTHING;
"

# Get Alice's UUID for track ownership
if [ "$EXEC_METHOD" == "local" ]; then
    export PGPASSWORD=$DB_PASS
    USER_ID=$(psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT uuid FROM users WHERE email='alice@mossaq.com';" | xargs)
else
    USER_ID=$(docker exec -i "$DB_CONTAINER" psql -U $DB_USER -d $DB_NAME -t -c "SELECT uuid FROM users WHERE email='alice@mossaq.com';" | tr -d '\r' | xargs)
fi

# 2. Upload Tracks (Direct DB Insert)
upload_track() {
    local title="$1"
    local artist="$2"
    local url="$3"
    local filename=$(basename "$url")

    echo "Downloading $filename..."
    curl -L -o "$filename" "$url" --silent

    if [ -f "$filename" ]; then
        echo "Processing $title..."
        
        # Prepare files
        local timestamp=$(date +%s)
        local stored_audio="${timestamp}_${filename}"
        local stored_image="${timestamp}_cover.jpg"
        
        # Move/Copy to uploads dir
        mv "$filename" "$UPLOADS_DIR/$stored_audio"
        cp "cover.jpg" "$UPLOADS_DIR/$stored_image"
        
        local abs_audio_path="$UPLOADS_DIR/$stored_audio"
        
        # Insert DB Record
        run_sql "INSERT INTO tracks (id, title, artist, user_id, filename, content_type, audio_file_path, image_file_path, play_count, like_count) VALUES (gen_random_uuid(), '$title', 'Alice', '$USER_ID', '$stored_audio', 'audio/mpeg', '$abs_audio_path', '$stored_image', 0, 0);"
    else
        echo -e "${RED}Failed to download $filename${NC}"
    fi
}

echo "Uploading 5 Tracks..."
upload_track "Synthwave Dreams" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
upload_track "Neon Nights" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
upload_track "Cyber Pulse" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
upload_track "Retro Future" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
upload_track "Deep Space" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"

# 3. Add Comments
echo "Adding Comments..."
# Get the last 5 track UUIDs from the database
if [ "$EXEC_METHOD" == "local" ]; then
    export PGPASSWORD=$DB_PASS
    TRACK_IDS=$(psql -h localhost -U $DB_USER -d $DB_NAME -t -c "SELECT id FROM tracks ORDER BY id DESC LIMIT 5;")
else
    TRACK_IDS=$(docker exec -i "$DB_CONTAINER" psql -U $DB_USER -d $DB_NAME -t -c "SELECT id FROM tracks ORDER BY id DESC LIMIT 5;")
fi

for track_id in $TRACK_IDS; do
    # Trim whitespace and carriage returns
    track_id=$(echo $track_id | tr -d '\r' | xargs)
    
    if [ -n "$track_id" ]; then
        # Insert 3 comments per track
        run_sql "
        INSERT INTO comments (id, content, author, track_id, timestamp) VALUES
        (gen_random_uuid(), 'This track is fire! 🔥', 'Alice', '$track_id', $(date +%s)000),
        (gen_random_uuid(), 'Love the bassline on this one.', 'Bob', '$track_id', $(date +%s)000),
        (gen_random_uuid(), 'Added to my playlist.', 'Charlie', '$track_id', $(date +%s)000);
        " > /dev/null
    fi
done

# Cleanup
rm "cover.jpg"

echo -e "${GREEN}Database populated successfully!${NC}"