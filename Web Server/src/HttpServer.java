
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

public class HttpServer {

    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
    private static final Map<String, String> users = new HashMap<>(); // username -> password
    private static final Map<String, String> roles = new HashMap<>(); // username -> role

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

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                } catch (Exception e) {
                    logger.severe("Error handling request: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.severe("Could not start server: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestLine = in.readLine();
        logger.info("Received request: " + requestLine);

        if (requestLine != null) {
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            if ("/".equals(path) && "GET".equals(method)) {
                handleRoot(out);
            } else if ("/login".equals(path) && "POST".equals(method)) {
                handleLogin(in, out);
            } else if ("/dashboard".equals(path) && "GET".equals(method)) {
                handleDashboard(out);
            } else if ("/users".equals(path) && "GET".equals(method)) {
                handleUsers(out);
            } else if ("/settings".equals(path) && "GET".equals(method)) {
                handleSettings(out);
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println();
                logger.warning("Unhandled path: " + path);
            }
        }

        in.close();
        out.close();
    }

    private static void handleRoot(PrintWriter out) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html>");
        out.println("<head><title>Welcome</title></head>");
        out.println("<body>");
        out.println("<h1>Welcome to the Simple Java Web Server</h1>");
        out.println("<p>Visit <a href='/dashboard'>/dashboard</a> for server administration.</p>");
        out.println("</body>");
        out.println("</html>");
        logger.info("Handled request for root path.");
    }

    private static void handleLogin(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null && !line.isEmpty()) {
            body.append(line).append("");
        }

        String[] credentials = body.toString().split("&");
        String username = credentials[0].split("=")[1];
        String password = credentials[1].split("=")[1];

        if (users.containsKey(username) && users.get(username).equals(password)) {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("Login successful! Welcome, " + username);
            logger.info("User logged in: " + username);
        } else {
            out.println("HTTP/1.1 401 Unauthorized");
            out.println();
            out.println("Invalid credentials");
            logger.warning("Failed login attempt for username: " + username);
        }
    }

    private static void handleDashboard(PrintWriter out) throws IOException {
        File file = new File("../config/dashboard.html").getCanonicalFile();
        logger.info("Looking for dashboard at: " + file.getAbsolutePath());

        if (file.exists()) {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println();

            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    out.println(line);
                }
            }
            logger.info("Dashboard served successfully.");
        } else {
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println();
            out.println("Dashboard file not found");
            logger.severe("Dashboard file not found at: " + file.getAbsolutePath());
        }
    }

    private static void handleUsers(PrintWriter out) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html>");
        out.println("<head><title>Manage Users</title></head>");
        out.println("<body>");
        out.println("<h1>Manage Users</h1>");
        out.println("<ul>");
        for (String user : users.keySet()) {
            out.println("<li>" + user + " - Role: " + roles.get(user) + "</li>");
        }
        out.println("</ul>");
        out.println("<a href='/dashboard'>Back to Dashboard</a>");
        out.println("</body>");
        out.println("</html>");
        logger.info("Served Manage Users page.");
    }

    private static void handleSettings(PrintWriter out) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html>");
        out.println("<head><title>Server Settings</title></head>");
        out.println("<body>");
        out.println("<h1>Server Settings</h1>");
        out.println("<p>Current Port: 8080</p>");
        out.println("<p>Max Connections: 100</p>");
        out.println("<a href='/dashboard'>Back to Dashboard</a>");
        out.println("</body>");
        out.println("</html>");
        logger.info("Served Server Settings page.");
    }
}
