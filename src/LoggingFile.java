import java.io.IOException;
import java.util.logging.*;

public class LoggingFile {
    private static final Logger logger = Logger.getLogger(LoggingFile.class.getName());

    public static void main(String[] args) {
        configureLogger();

        logger.info("Logging Started");
    }

    private static void configureLogger() {
        try {
            String logFileName = "logs/app-" + System.currentTimeMillis() + ".log";
            FileHandler fileHandler = new FileHandler(logFileName, true);

            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%1$tF %1$tT] [%2$s] %3$s %n",
                            record.getMillis(),
                            record.getLevel().getName(),
                            record.getMessage());
                }
            });

            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING);
            logger.addHandler(consoleHandler);

            logger.info("Logger configured successfully.");
        } catch (IOException e) {
            System.err.println("Failed to log: " + e.getMessage());
        }
    }
}
