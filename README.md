# Movies Recommendation

This is a JavaFX-based Movie Recommendation application.

## Project Overview

The application allows users to discover movies. It appears to include features for finding movies based on "vibes" or other criteria, utilizing a backend service or API for movie data.

## Collaborators

This project is a collaboration by:
*   ewmw1221
*   haymanot13
*   bilalx570
*   haftam8567
*   kanu5858
*   amhefm

## Tech Stack

*   **Language:** Java 21
*   **Build Tool:** Maven
*   **UI Framework:** JavaFX 21
*   **Dependencies:**
    *   `org.openjfx:javafx-controls`
    *   `org.openjfx:javafx-fxml`
    *   `org.openjfx:javafx-web`
    *   `org.controlsfx:controlsfx`
    *   `org.kordamp.bootstrapfx:bootstrapfx-core`
    *   `com.google.code.gson:gson` (JSON parsing)
    *   `org.glassfish.jersey.core:jersey-client` (HTTP Client)

## Project Structure

The source code is located in `src/main/java/com/example/moviesrecommendation`.

Key classes include:
*   `HelloApplication.java`: The main entry point for the JavaFX application.
*   `HelloController.java`: Controller for the main view.
*   `VibeFinderController.java`: Controller for the vibe finder functionality.
*   `Movie.java`: Data model for a movie.
*   `MovieApi.java` & `MovieApiServer.java`: Handling API interactions.
*   `MovieService.java`: Service layer logic.

## How to Run

1.  Ensure you have Java 21 installed.
2.  Navigate to the project directory.
3.  Run the application using Maven:

```bash
mvn clean javafx:run
```

## Requirements

*   Java JDK 21
*   Maven
