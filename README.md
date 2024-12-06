
# Simple Java Web Server

This project implements a simple Java-based web server with logging, authentication, authorization, and a basic server administration interface.

## Features

1. **HTTP Methods**:
   - Supports `GET`, `HEAD`, and `POST` requests.

2. **Authentication**:
   - Users can log in using a hardcoded username and password via the `/login` endpoint.

3. **Authorization**:
   - Role-based access control is implemented. Users are assigned roles (`admin` or `user`).

4. **Server Administration Interface**:
   - A basic web-based dashboard is accessible at `/dashboard`.

5. **Logging**:
   - Server activity is logged to both a file and the console.

## Project Structure

```
project/
├── src/
│   ├── LoggingFile.java
│   ├── SimpleHttpServer.java
├── config/
│   ├── logconfig.xml
│   ├── dependencies.xml
│   ├── dashboard.html
├── out/  (generated after compilation)
```

## Prerequisites

- Java Development Kit (JDK) version 8 or later.
- A terminal or command prompt to compile and run the server.

## How to Run

1. **Clone or Download the Project**:
   - Download the zip file and extract its contents.

2. **Compile the Java Files**:
   ```bash
   javac -d out src/*.java
   ```

3. **Run the Server**:
   ```bash
   java -cp out HttpServer
   ```

4. **Access the Server**:
   - Open your browser and navigate to `http://localhost:8080`.

5. **Endpoints**:
   - `/login` (POST): Authenticate with username and password.
   - `/dashboard` (GET): Access the server administration interface.

## Default Users

- **Admin**:
  - Username: `admin`
  - Password: `adminpass`
- **User**:
  - Username: `user`
  - Password: `userpass`

## Notes

- Configuration files are located in the `config/` directory.
- Logs are generated in the `logs/` directory.

## Future Improvements

- Integrate dynamic user management.
- Add more endpoints for server configuration.

## Author Contributions

- **Han Le**: HTTP methods (`GET`, `HEAD`, `POST`).
- **Kenny Shi**: Logging.
- **Qiwen Zhu**: Authentication.
- **Qiyu Liu**: Authorization.
- **Yitao Zhou**: Server administration interface.
