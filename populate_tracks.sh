#!/bin/bash

# Mossaq Data Population Script
# This script downloads sample tracks and uploads them to the running Mossaq application.

APP_URL="http://localhost:8080"
UPLOAD_ENDPOINT="$APP_URL/track/upload"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Checking if Mossaq is running..."
if ! curl --output /dev/null --silent --head --fail "$APP_URL"; then
    echo -e "${RED}Error: Mossaq application is not reachable at $APP_URL${NC}"
    echo "Please start the application using './mvnw spring-boot:run' in a separate terminal."
    exit 1
fi

upload_track() {
    local title="$1"
    local artist="$2"
    local url="$3"
    local filename=$(basename "$url")

    echo "--------------------------------------------------"
    echo "Processing: $title - $artist"
    echo "Downloading $filename..."
    
    # Download with curl, following redirects
    curl -L -o "$filename" "$url" --silent

    # Check if file exists
    if [ ! -f "$filename" ]; then
        echo -e "${RED}Failed to download file.${NC}"
        return
    fi

    # Check if file is empty or too small (under 10KB implies a broken download or error page)
    filesize=$(wc -c <"$filename" | xargs)
    if [ "$filesize" -lt 10000 ]; then
        echo -e "${RED}File too small ($filesize bytes). Download likely failed.${NC}"
        return
    fi

    echo "Uploading to Mossaq..."
    # Upload using multipart/form-data
    response=$(curl -X POST -F "title=$title" -F "artist=$artist" -F "file=@$filename" "$UPLOAD_ENDPOINT" -w "%{http_code}" -o /dev/null -s)

    if [ "$response" -eq 200 ]; then
        echo -e "${GREEN}Success! Uploaded.$NC"
    else
        echo -e "${RED}Upload failed with status code: $response${NC}"
    fi

    # Clean up local file
    rm "$filename"
}

# Sample Data from SoundHelix (Creative Commons)
upload_track "Synthwave Dreams" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
upload_track "Neon Nights" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
upload_track "Cyber Pulse" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
upload_track "Retro Future" "SoundHelix" "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"

echo "--------------------------------------------------"
echo -e "${GREEN}Population complete! Visit $APP_URL/main to see the tracks.${NC}"