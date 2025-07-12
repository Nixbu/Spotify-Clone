# Spotify Clone Web Application

This project is a web application that emulates some of the core functionalities of the popular music streaming service, Spotify. It is built using the Spring Boot framework for the backend and Thymeleaf with Bootstrap for the frontend. Users can register, upload their own music, create albums, manage playlists, and listen to music through a web-based player.

## data and users ##
*  we provided an sql file containing some data:
*  sql file path - src/main/java/com.example.spotifyclone/data/ex4.sql
*  in this database there are 2 users registered- 
*  username - itay password 123456
*  username - yosi password 123456
*  those users share a playlist.
*  we also uploaded a package called - uploades ,in this package we save the actual mp3 files ( in the datat base we save only the paths ).
*  if you want to start from scratch ( meaning uploading your own mp3 files) you can just delete this package.

## Table of Contents
1.  [General Functionality](#general-functionality)
2.  [Screenshots](#screenshots)
3.  [Technologies Used](#technologies-used)
4.  [Setup and Installation](#setup-and-installation)
5.  [SQL File Information](#sql-file-information)
6.  [Credentials](#credentials)
7.  [Known Bugs and Limitations](#known-bugs-and-limitations)
8.  [Project Structure](#project-structure)

---

## General Functionality

The application provides a rich set of features for a music streaming platform.

## 🎬 Demo Video

A short video presentation of the application:

[▶️ Watch the demo video on Google Drive](https://drive.google.com/file/d/13UwqzLOIpav4RpqyUor9IAa-VL1oQcVU/view?usp=sharing)


### User Management
* **Authentication:** Users can create an account, log in, and log out. The system uses Spring Security for handling authentication and authorization.
* **Registration:** A secure registration form with validation for username, email, and password.

### Music and Content
* **Song Uploading:** Authenticated users can upload their own MP3 files. Each song includes details like title, artist, and genre.
* **Album Creation:** Users can create albums, upload cover art, and associate their uploaded songs with these albums.
* **Dynamic Content Browsing:** Users can browse and search for all available songs and albums in the system.

### Playlists and Favorites
* **Playlist Management:** Users can create personal playlists, add songs to them, and remove songs.
* **Collaborative Playlists:** Playlists can be shared with other registered users, allowing them to view and modify the playlist.
* **Liked Songs:** Users have a personal "Favorites" list where they can add or remove songs with a single click.

### Music Player
* **Web-Based Player:** A fully functional music player that can stream songs directly from the server.
* **Player Controls:** Includes standard controls like play/pause, next/previous track, a progress bar, volume control, and playback speed adjustment.
* **Queue Management:** The player displays the current queue, whether it's an album, a playlist, or the user's liked songs.
* **Shuffle and Repeat:** Users can shuffle the current queue or set a track to repeat.

---

## Screenshots

Here are some placeholders where you can insert images of the live application.

**1. Home Page**
*A view of the main landing page for both guest and authenticated users.*
![Home Page](screenshots/home.png)

**2. Login Page**
*The user login form.*
![Login Page](screenshots/login.png)

**3. Songs Library**
*A view of the main songs page with search and filter options.*
![Songs Page](screenshots/songs.png)

**4. Album Details Page**
*A detailed view of a single album, listing its tracks.*
![Album Details Page](screenshots/album-details.png)

**5. Playlist Details Page**
*A view of a user's playlist, showing songs and sharing options.*
![Playlist Details Page](screenshots/sharing-playlists.png)

**6. Music Player Interface**
*The main music player UI in action.*
![Music Player](screenshots/player.png)

---

## Technologies Used

* **Backend:**
    * Java 23
    * Spring Boot
    * Spring Security
    * Spring Data JPA (Hibernate)
    * Maven
* **Frontend:**
    * HTML5
    * CSS3 (with Bootstrap 5)
    * JavaScript (ES6)
    * Thymeleaf
* **Database:**
    * MySQL

---

## Setup and Installation

Follow these steps to get the project running on your local machine.

### Prerequisites
* JDK 23 or later
* Apache Maven
* MySQL Server

### 1. Database Configuration
1.  Ensure your MySQL server is running.
2.  Create a new database for the project. The default name in the configuration is `ex4`. You can create it with the following SQL command:
    ```sql
    CREATE DATABASE ex4;
    ```
3.  Open the `src/main/resources/application.properties` file.
4.  Update the database URL, username, and password to match your local MySQL setup:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/ex4?createDatabaseIfNotExist=true
    spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password
    ```

### 2. Build and Run the Application
1.  Navigate to the root directory of the project in your terminal.
2.  Build the project using Maven:
    ```bash
    mvn clean install
    ```
3.  Run the application:
    ```bash
    mvn spring-boot:run
    ```
4.  The application should now be running on `http://localhost:8080`.

---

## SQL File Information

**No `.sql` file is required for import.**

The project is configured with `spring.jpa.hibernate.ddl-auto=update` in the `application.properties` file. This means that Hibernate will automatically create and update the database schema based on the entity classes (`@Entity`) defined in the project. When you run the application for the first time, all necessary tables will be generated in the configured database.

---

## Credentials

There is no default admin or user account. You can create a new user account by navigating to the registration page:

* **URL:** `http://localhost:8080/register`

Simply fill out the registration form to create an account and start using the application.

---

## Known Bugs and Limitations

* **Favorites button color** The color of the favorites button goes back to not being red after reloading the page even
                            though the song is still favorited.
* **Album deletion** Albums can be deleted by other users who did not upload it.

## Project Structure

The project follows a standard Spring Boot application structure.

* `src/main/java/com/example/spotifyclone`: Main package root.
    * `config`: Spring Security and Web MVC configurations.
    * `controller`: Handles incoming web requests.
    * `entity`: JPA entity classes that map to database tables.
    * `repository`: Spring Data JPA repositories for database operations.
    * `service`: Contains the business logic.
    * `dto`: Data Transfer Objects for API responses.
* `src/main/resources`:
    * `static`: CSS, JavaScript, and image files.
    * `templates`: Thymeleaf HTML templates.
    * `application.properties`: Application configuration.
* `uploads/`: (Created at runtime) Stores uploaded MP3s and cover images.
