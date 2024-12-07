import java.io.IOException;
import java.util.logging.*;

public class LoggingFile {

    // Logger instance for managing log messages
    private static final Logger logger = Logger.getLogger(LoggingFile.class.getName());

    public static void main(String[] args) {
        configureLogger(); // Configure logger with handlers

        logger.info("Logging Started"); // Log a startup message
    }

    // Configures the logger with file and console handlers
    private static void configureLogger() {
        try {
            // Generate a log file name with a timestamp
            String logFileName = "logs/app-" + System.currentTimeMillis() + ".log";

            // Create a file handler for logging to a file
            FileHandler fileHandler = new FileHandler(logFileName, true);

            // Define a custom formatter for the log messages
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%1$tF %1$tT] [%2$s] %3$s %n",
                            record.getMillis(),         // Timestamp
                            record.getLevel().getName(), // Log level (e.g., INFO, WARNING)
                            record.getMessage());        // Log message
                }
            });

            // Add the file handler to the logger
            logger.addHandler(fileHandler);

            // Set logger level to capture all log levels
            logger.setLevel(Level.ALL);

            // Create a console handler for logging to the console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING); // Log only warnings and above to console

            // Add the console handler to the logger
            logger.addHandler(consoleHandler);

            logger.info("Logger configured successfully."); // Log a success message
        } catch (IOException e) {
            System.err.println("Failed to log: " + e.getMessage()); // Handle any errors
        }
    }
}
