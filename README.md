# Mossaq 🎵

**Stream, Share, & Connect**

Mossaq is a comprehensive music streaming and social platform. It bridges the gap between artists and listeners, allowing users to upload their own tracks, curate playlists, and connect with friends through music.

## 🚀 Features

### 🎧 Streaming & Playback
*   **Seamless Audio Streaming**: Stream `.mp3` and `.wav` files directly from the browser.
*   **Immersive Player**: Custom-styled audio player with album art display.
*   **Track Art**: Support for custom cover art uploads.

### 🤝 Social & Community
*   **Friend System**:
    *   **Search**: Find users by name.
    *   **Connect**: Send, accept, and decline friend requests.
    *   **Network**: View and manage your friends list.
*   **Private Sharing**: Share tracks directly with your friends. Access them in the "Shared with Me" section.
*   **Engagement**: Like your favorite tracks and join the conversation in the comments section.
*   **User Profiles**:
    *   **Public**: View other users' uploads and bios.
    *   **Personal**: Customize your avatar, bio, and account settings.

### 📂 Library & Management
*   **Music Upload**: Creators can upload tracks with titles, custom artist names, and cover images.
*   **Personal Playlists**: Build your own library by adding tracks to "My Playlist".
*   **Content Management**: Users have full control to delete their uploaded tracks.
*   **Local Sync**: Scripts to populate the database directly from a local `uploads/` folder.

## 🛠️ Tech Stack

*   **Backend**: Java 25, Spring Boot 4.0.1 (Web, Data JPA, Security)
*   **Frontend**: Thymeleaf, HTML5, CSS3, Vanilla JavaScript
*   **Database**: PostgreSQL (with `pgcrypto` for UUID generation)
*   **Build Tool**: Maven

## 🏁 Getting Started

### Prerequisites

*   Java 25 SDK installed.
*   Docker & Docker Compose (optional, for running the database).

### Database Setup

You can start a PostgreSQL instance easily using Docker Compose:

```bash
docker-compose up -d
```

### Running the Application

1.  Clone the repository.
2.  Navigate to the project directory.
3.  Run the application using the Maven wrapper:

    ```bash
    ./mvnw spring-boot:run
    ```

5.  Open your browser and navigate to:

    ```
    http://localhost:8080
    ```

### Populating the Database

To seed the database with test users and tracks, you can use the provided scripts.

*   **Standard Population**: Creates users and uploads sample tracks via the API (requires the app to be running).
    ```bash
    ./populate_db.sh
    ```
*   **Local Population**: Scans the `uploads/` folder and registers existing MP3 files directly into the database.
    ```bash
    ./populate_from_uploads.sh
    ```

## 📄 License

&copy; 2026 Mossaq Music. All rights reserved.