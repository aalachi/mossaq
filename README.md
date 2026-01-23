# Mossaq 🎵

**Stream, Share, & Connect**

Mossaq is a comprehensive music streaming and social platform. It bridges the gap between artists and listeners, allowing users to upload their own tracks, curate playlists, and connect with friends through music.

## 🚀 Features

### 🎧 Streaming & Playback
*   **Seamless Audio Streaming**: Stream `.mp3` and `.wav` files directly from the browser.
*   **Visual Player**: Interactive audio player with dynamic waveform visualizations.
*   **Track Art**: Support for custom cover art uploads.

### 🤝 Social & Community
*   **Friend System**: Search for users, send friend requests, and manage your friends list.
*   **Private Sharing**: Share tracks directly with your friends. Access them in the "Shared with Me" section.
*   **Engagement**: Like your favorite tracks and join the conversation in the comments section.
*   **User Profiles**: Customizable profiles with avatars, bios, and track catalogs.

### 📂 Library & Management
*   **Music Upload**: Creators can easily upload tracks with titles and cover images.
*   **Personal Playlists**: Build your own library by adding tracks to "My Playlist".
*   **Content Management**: Users have full control to delete their uploaded tracks or their entire account.

## 🛠️ Tech Stack

*   **Backend**: Java 25, Spring Boot 4.0.1 (Web, Data JPA, Security)
*   **Frontend**: Thymeleaf, HTML5, CSS3, Vanilla JavaScript
*   **Database**: PostgreSQL (with `pgcrypto` for UUID generation)
*   **Build Tool**: Maven

## 🏁 Getting Started

### Prerequisites

*   Java 25 SDK installed.
*   PostgreSQL database running.

### Running the Application

1.  Clone the repository.
2.  Navigate to the project directory.
3.  Configure your database settings in `application.properties`.
4.  Run the application using the Maven wrapper:

    ```bash
    ./mvnw spring-boot:run
    ```

5.  Open your browser and navigate to:

    ```
    http://localhost:8080
    ```

## 📄 License

&copy; 2026 Mossaq Music. All rights reserved.