package calibreautomations;

import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class CalibreUpdater {
    private static final Logger logger = LoggerFactory.getLogger(CalibreUpdater.class);
    private static String DB_URL;
    private static final Gson gson = new Gson();

    private final Connection connection;
    private final CalibreDB calibredb;

    public CalibreUpdater(Connection connection) {
        this.connection = connection;
        this.calibredb = new CalibreDB(connection);
    }

    public static void main(String[] args) {
        loadConfiguration();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            CalibreUpdater calibreUpdater = new CalibreUpdater(conn);
            calibreUpdater.run(args);
        } catch (SQLException e) {
            logger.error("Database connection error", e);
        }
    }

    private static void loadConfiguration() {
        Properties properties = new Properties();
        try (InputStream input = CalibreUpdater.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
            DB_URL = properties.getProperty("db.url");
            String logLevel = properties.getProperty("log.level");
            setLogLevel(logLevel);
        } catch (IOException ex) {
            logger.error("Error loading configuration", ex);
        }
    }

    private static void setLogLevel(String logLevel) {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        switch (logLevel.toUpperCase()) {
            case "DEBUG":
                rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
                break;
            case "INFO":
                rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);
                break;
            case "WARN":
                rootLogger.setLevel(ch.qos.logback.classic.Level.WARN);
                break;
            case "ERROR":
                rootLogger.setLevel(ch.qos.logback.classic.Level.ERROR);
                break;
            default:
                rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);
                break;
        }
    }

    public void run(String[] args) {
        AppOptions options = new AppOptions();

        try {
            options.parse(args);
        } catch (ParseException e) {
            logger.error("Error parsing command line options", e);
            System.out.println(e.getMessage());
            System.out.println(options.help());
            return;
        }

        try {
            updateCalibre(options);
        } catch (SQLException e) {
            logger.error("Error updating Calibre", e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    protected void updateCalibre(AppOptions options) throws SQLException {
        String readOrderTable = calibredb.getReadOrderTable();
        if (options.isReadorders() && readOrderTable == null) {
            System.out.println("ERROR: Custom field 'readorder' not found in the Calibre database.");
            return;
        }
        List<Book> books = calibredb.getBooks(readOrderTable);
        int numItemsUpdated = 0;
        int numAudiobooksUpdated = 0;
        int numReadordersUpdated = 0;
        boolean audiobookUpdated = false;
        boolean readorderUpdated = false;
        for (Book book : books) {
            // TODO Validate that there is only one format (or more than one?), Validate that there is one
            // TODO Check if a finished book has a score
            if (options.isAudiobooks()) {
                audiobookUpdated = processAudiobook(options.isDryRun(), book);
            }
            if (options.isReadorders()) {
                readorderUpdated = processReadOrder(options.isDryRun(), book);
            }
            numAudiobooksUpdated += audiobookUpdated ? 1 : 0;
            numReadordersUpdated += readorderUpdated ? 1 : 0;
            numItemsUpdated += audiobookUpdated || readorderUpdated ? 1 : 0;
        }
        String updateMessage = options.isDryRun() ? "to update (dry-run)" : "updated";
        System.out.printf("%d items %s (%d audiobook changes, %d readorder changes)%n", numItemsUpdated, updateMessage, numAudiobooksUpdated, numReadordersUpdated);
    }

    private boolean processAudiobook(boolean dryRun, Book book) {
        // TODO Consider dry-run for Audiobook
        // TODO count updated books, and updated books for audiobook or for readorder
        boolean itemUpdated = false;
        if (book.isAudioBookFromTags()) {
            if (!book.getTitle().contains("(audiobook)")) {
                System.out.printf("%-30s for \"%s\" to ...%n", "[add audiobook to title]", book.getTitle());
                itemUpdated = true;
            }
        }
        if (book.getTitle().contains("(audiobook)")) {
            if (!book.isAudioBookFromTags()) {
                System.out.printf("%-30s for \"%s\" to ...%n", "[remove audiobook from title]", book.getTitle());
                itemUpdated = true;
            }
        }
        return itemUpdated;
    }


    private boolean processReadOrder(boolean dryRun, Book book) {
        String readOrderFromTags = book.getFirstReadOrderFromTags();
        String readOrderFromCustomField = book.getReadOrderFromCustomField();
        boolean itemUpdated = false;
        // Case 1: More than one readorder tag, we keep the first one
        if (book.getNumReadOrdersFromTags() > 1) {
            System.out.printf("%-30s for \"%s\" to ...%n", "[remove extra readorder tags]", book.getTitle());
            // TODO Implement book.removeExtraReadOrderTags();
            //book.removeExtraReadOrderTags();
            itemUpdated = true;
        }
        // Case 2: No readorder tag but custom field exists, we add the tag
        if (book.getNumReadOrdersFromTags() == 0 && !readOrderFromCustomField.equals("no-readorder")) {
            System.out.printf("%-30s for \"%s\" to ...%n", "[add readorder tag]", book.getTitle());
            // TODO Implement book.addReadOrderTag(readOrderFromCustomField);
            //book.addReadOrderTag(readOrderFromCustomField);
            itemUpdated = true;
        }
        // Case 3: Mismatch between tag and custom field, custom field wins
        else if (!readOrderFromTags.equals("no-readorder") && !readOrderFromCustomField.equals("no-readorder") && !readOrderFromTags.equals(readOrderFromCustomField)) {
            System.out.printf("%-30s for \"%s\" from \"%s\" to \"%s\"%n", "[update readorder]", book.getTitle(), readOrderFromTags, readOrderFromCustomField);
            // TODO Update readorder tag to match custom field
            //book.updateReadOrderTag(readOrderFromCustomField);
            itemUpdated = true;
        }
        // Case 4: Delete custom field if value is 0.0
        if (readOrderFromCustomField.equals("0.0")) {
            System.out.printf("%-30s for \"%s\" to ...%n", "[delete readorder]", book.getTitle());
            // TODO Implement book.deleteReadOrderCustomField();
            //calibredb.deleteReadOrderCustomField();
            itemUpdated = true;
        }
        // Case 5: Delete readorder tag if custom field is empty
        // Skip the update if there are no changes to the tags
        if (!readOrderFromTags.equals(readOrderFromCustomField)) {
            if (!dryRun) {
                // TODO Update the database will require delete fields, as in the python code
                //updateReadOrder(conn, book, readOrderFromCustomField, readOrderTable);
            }
            System.out.printf("%-30s for \"%s\" from \"%s\" to \"%s\"%n", "[update readorder]", book.getTitle(), readOrderFromTags, readOrderFromCustomField);
            itemUpdated = true;
        }
        return itemUpdated;
    }
}