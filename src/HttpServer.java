import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;
import java.nio.charset.StandardCharsets;

public class HttpServer {

    // Logger instance for logging server events
    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());

    // In-memory storage for user credentials and roles
    private static final Map<String, String> users = new HashMap<>(); // username -> password
    private static final Map<String, String> roles = new HashMap<>(); // username -> role

    // Static block to initialize default users and roles
    static {
        // Default users
        users.put("admin", "adminpass");
        roles.put("admin", "admin"); // Admin user

        users.put("user", "userpass");
        roles.put("user", "user"); // Regular user
    }

    public static void main(String[] args) {
        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port " + port);

            // Accept and handle client connections in separate threads
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        handleRequest(clientSocket); // Handle client request
                    } catch (IOException e) {
                        logger.severe("Error handling request: " + e.getMessage());
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            logger.severe("Error closing client socket: " + e.getMessage());
                        }
                    }
                }).start(); // Start thread for each client
            }

        } catch (IOException e) {
            logger.severe("Could not start server: " + e.getMessage());
        }
    }
    // Handles incoming HTTP requests
    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestLine = in.readLine();
        logger.info("Received request: " + requestLine);

        if (requestLine != null) {
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            // Route the request to the appropriate handler
            if (path.startsWith("/users/delete") && "GET".equals(method)) {
                handleDeleteUser(path, out);
            } else if ("/".equals(path) && "GET".equals(method)) {
                handleRoot(out);
            } else if ("/login".equals(path) && "POST".equals(method)) {
                handleLogin(in, out);
            } else if ("/dashboard".equals(path) && "GET".equals(method)) {
                handleDashboard(out);
            } else if ("/users".equals(path) && "GET".equals(method)) {
                handleUsers(out);
            } else if ("/users/add".equals(path) && "POST".equals(method)) {
                handleAddUser(in, out, clientSocket);
            } else {
                sendHttpResponse(out, "404 Not Found", "text/plain", "404 Not Found");
                logger.warning("Unhandled path: " + path);
            }
        }

        in.close();
        out.close();
    }

    // Handles the root endpoint
    private static void handleRoot(PrintWriter out) {
        String response = "<html>"
                + "<head><title>Welcome</title></head>"
                + "<body>"
                + "<h1>Welcome to the Simple Java Web Server</h1>"
                + "<p>Visit <a href='/users'>/users</a> for server administration.</p>"
                + "</body>"
                + "</html>";
        sendHttpResponse(out, response);
        logger.info("Handled request for root path.");
    }

    // Handles user login requests
    private static void handleLogin(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;

        // Read request body for login credentials
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            body.append(line).append("\n");
        }

        // Extract username and password from the body
        String[] credentials = body.toString().split("&");
        if (credentials.length < 2) {
            sendHttpResponse(out, "Invalid login data", "text/plain", "400 Bad Request");
            logger.warning("Invalid login data received");
            return;
        }

        String username = URLDecoder.decode(credentials[0].split("=")[1], StandardCharsets.UTF_8);
        String password = URLDecoder.decode(credentials[1].split("=")[1], StandardCharsets.UTF_8);

        // Authenticate user credentials
        String response;
        if (users.containsKey(username) && users.get(username).equals(password)) {
        response = String.format(
         "<!DOCTYPE html>" +
        "<html lang=\"en\">" +
        "<head>" +
        "    <meta charset=\"UTF-8\">" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
        "    <title>%s</title>" +
        "    <style>" +
        "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f9; color: #333; display: flex; justify-content: center; align-items: center; height: 100vh; }" +
        "        .container { text-align: center; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }" +
        "        .success { color: green; }" +
        "    </style>" +
        "</head>" +
        "<body>" +
        "    <div class=\"container\">" +
        "        <h1>%s</h1>" +
        "        <p class=\"success\">%s</p>" +
        "    </div>" +
        "</body>" +
        "</html>",
        "Login Successful", "Welcome!", "Welcome, " + username
    );
    logger.info("User logged in: " + username);
    sendHttpResponse(out, response, "text/html");
} else {
    response = String.format(
        "<!DOCTYPE html>" +
        "<html lang=\"en\">" +
        "<head>" +
        "    <meta charset=\"UTF-8\">" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
        "    <title>%s</title>" +
        "    <style>" +
        "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f9; color: #333; display: flex; justify-content: center; align-items: center; height: 100vh; }" +
        "        .container { text-align: center; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }" +
        "        .error { color: red; }" +
        "    </style>" +
        "</head>" +
        "<body>" +
        "    <div class=\"container\">" +
        "        <h1>%s</h1>" +
        "        <p class=\"error\">%s</p>" +
        "    </div>" +
        "</body>" +
        "</html>",
        "Login Failed", "Invalid credentials, please try again."
    );
    logger.warning("Failed login attempt for username: " + username);
    sendHttpResponse(out, response, "text/html", "401 Unauthorized");
}

    }
    // Handles the dashboard endpoint
    private static void handleDashboard(PrintWriter out) throws IOException {
        File file = new File("../config/dashboard.html").getCanonicalFile();
        logger.info("Looking for dashboard at: " + file.getAbsolutePath());

        if (file.exists()) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    response.append(line).append("\n");
                }
            }
            sendHttpResponse(out, response.toString(), "text/html");
            logger.info("Dashboard served successfully.");
        } else {
            String response = "Dashboard file not found";
            sendHttpResponse(out, response, "text/plain", "500 Internal Server Error");
            logger.severe("Dashboard file not found at: " + file.getAbsolutePath());
        }
    }
    
    // Handles additional methods (e.g., /users, /users/add, /users/delete)
    // Each method uses appropriate responses based on path and method
    private static void handleUsers(PrintWriter out) {
        StringBuilder response = new StringBuilder();
        response.append("<html>")
                .append("<head><title>Manage Users</title></head>")
                .append("<body>")
                .append("<h1>Manage Users</h1>")
                .append("<ul>");
        for (String user : users.keySet()) {
            response.append("<li>").append(user).append(" - Role: ").append(roles.get(user))
                    .append(" - <a href='/users/delete?user=").append(user).append("'>Delete</a></li>");
        }
        response.append("</ul>")
                .append("<form action='/users/add' method='post'>")
                .append("<h3>Add User</h3>")
                .append("Username: <input type='text' name='username'><br>")
                .append("Password: <input type='text' name='password'><br>")
                .append("<input type='submit' value='Add User'>")
                .append("</form>")
                .append("<a href='/'>Back to Dashboard</a>")
                .append("</body>")
                .append("</html>");
        sendHttpResponse(out, response.toString());
        logger.info("Served Manage Users page.");
    }

    private static void handleAddUser(BufferedReader in, PrintWriter out, Socket clientSocket) throws IOException {
        int contentLength = 0;
        String headerLine;

        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            if (headerLine.startsWith("Content-Length: ")) {
                contentLength = Integer.parseInt(headerLine.substring("Content-Length: ".length()));
            }
        }

        char[] bodyChars = new char[contentLength];
        in.read(bodyChars);
        String body = new String(bodyChars);

        String[] params = body.split("&");
        if (params.length < 2) {
            sendHttpResponse(out, "Invalid user data", "text/plain", "400 Bad Request");
            logger.warning("Invalid user data received");
            return;
        }

        String username = URLDecoder.decode(params[0].split("=")[1], StandardCharsets.UTF_8);
        String password = URLDecoder.decode(params[1].split("=")[1], StandardCharsets.UTF_8);

        String response;
        if (!username.isEmpty() && !password.isEmpty()) {
            users.put(username, password);
            roles.put(username, "user");
            response = "<html><body><h1>User Added Successfully</h1><a href='/users'>Back to Users</a></body></html>";
            logger.info("User added: " + username);
        } else {
            response = "Invalid input for adding user";
            logger.warning("Failed to add user: Invalid input");
        }
        sendHttpResponse(out, response);
    }

    private static void handleDeleteUser(String path, PrintWriter out) throws IOException {
        String query = path.split("\\?")[1];
        String[] params = query.split("=");
        if (params.length < 2 || !"user".equals(params[0])) {
            sendHttpResponse(out, "Invalid delete data", "text/plain", "400 Bad Request");
            logger.warning("Invalid delete data received");
            return;
        }

        String username = URLDecoder.decode(params[1], StandardCharsets.UTF_8);

        String response;
        if (users.containsKey(username)) {
            users.remove(username);
            roles.remove(username);
            response = "<html><body><h1>User Deleted Successfully</h1><a href='/users'>Back to Users</a></body></html>";
            logger.info("User deleted: " + username);
        } else {
            response = "User not found";
            logger.warning("Failed to delete user: " + username + " not found");
        }
        sendHttpResponse(out, response);
    }

    private static void sendHttpResponse(PrintWriter out, String response) {
        sendHttpResponse(out, response, "text/html");
    }

    private static void sendHttpResponse(PrintWriter out, String response, String contentType) {
        sendHttpResponse(out, response, contentType, "200 OK");
    }

    private static void sendHttpResponse(PrintWriter out, String response, String contentType, String status) {
        out.println("HTTP/1.1 " + status);
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + response.getBytes().length);
        out.println("Connection: close");
        out.println();
        out.println(response);
        out.flush();
    }
}
