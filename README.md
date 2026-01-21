# Mossaq 🎵

**Stream & Share Your Sound**

Mossaq is a music library and streaming platform designed for everyone. Whether you are a listener looking to stream millions of songs or an artist wanting to showcase your talent to the world, Mossaq provides the stage.

## 🚀 Features

*   **User Authentication**: Secure registration and login functionality.
*   **Music Dashboard**: Browse and listen to available tracks.
*   **Audio Streaming**: Stream uploaded tracks directly in the browser.
*   **Easy Uploads**: Upload audio files with metadata (Title, Artist).
*   **Local Storage**: Files are securely stored on the server.

## 🛠️ Tech Stack

*   **Java 25**
*   **Spring Boot 4.0.1**
*   **Spring Security**
*   **Spring Data JPA**
*   **PostgreSQL**
*   **Thymeleaf** (Frontend Templating)
*   **Spring Web MVC**
*   **Maven**

## 🏁 Getting Started

### Prerequisites

*   Java 25 SDK installed.
*   PostgreSQL database.

### Running the Application

1.  Clone the repository.
2.  Navigate to the project directory.
3.  Configure your database connection in `application.properties`.
4.  Run the application using the Maven wrapper:

    ```bash
    ./mvnw spring-boot:run
    ```

5.  Open your browser and navigate to:

    ```
    http://localhost:8080
    ```

### 🧪 Sample Data

You can populate the application with sample tracks using the included script:

```bash
./populate_tracks.sh
```

## 📄 License

&copy; 2026 Mossaq Music. All rights reserved.