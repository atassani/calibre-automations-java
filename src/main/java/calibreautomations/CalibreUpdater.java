package calibreautomations;

import calibreautomations.persistence.CalibreDB;
import calibreautomations.persistence.CalibreDBJdbc;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class CalibreUpdater {
    public static final String NO_READORDER = "no-readorder";
    private static final Logger logger = LoggerFactory.getLogger(CalibreUpdater.class);
    private static String DB_URL;
    private final Connection connection;
    private final CalibreDB calibredb;

    public CalibreUpdater(Connection connection, CalibreDB calibredb) {
        this.connection = connection;
        this.calibredb = calibredb;
    }

    public static void main(String[] args) {
        loadConfiguration();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            CalibreDB calibreDB = new CalibreDBJdbc(conn);
            CalibreUpdater calibreUpdater = new CalibreUpdater(conn, calibreDB);
            calibreUpdater.run(args);
        } catch (SQLException e) {
            logger.error("Database connection error", e);
        }
    }

    private static void loadConfiguration() {
        Properties properties = new Properties();
        String configFileName = System.getProperty("test.mode") != null ? "config-test.properties" : "config.properties";
        try (InputStream input = CalibreUpdater.class.getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                logger.error("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
            DB_URL = properties.getProperty("db.url");
        } catch (IOException ex) {
            logger.error("Error loading configuration", ex);
        }
    }

    public void run(String[] args) {
        AppOptions options = new AppOptions();

        try {
            options.parse(args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println(options.help());
            return;
        }

        try {
            updateCalibre(options);
        } catch (IllegalStateException e) {
            logger.error("ERROR: Custom field 'readorder' not found in the Calibre database.");
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
        List<Book> books = calibredb.getBooks();
        int numItemsUpdated = 0;
        int numAudiobooksUpdated = 0;
        int numReadordersUpdated = 0;
        for (Book book : books) {
            boolean audiobookUpdated = false;
            boolean readorderUpdated = false;
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

    boolean processAudiobook(boolean dryRun, Book book) throws SQLException {
        // TODO Consider dry-run for Audiobook
        boolean itemUpdated = false;
        if (book.isAudioBookFromTags()) {
            if (!book.getTitle().contains("(audiobook)")) {
                String title = book.getTitle();
                if (title.contains(":")) {
                    title = title.replaceFirst(":", " (audiobook):");
                } else {
                    title = title + " (audiobook)";
                }
                System.out.printf("%-30s for \"%s\" to \"%s\"%n", "[add audiobook to title]", book.getTitle(), title);
                if (!dryRun) {
                    calibredb.updateBookTitle(book.getId(), title);
                }
                itemUpdated = true;
            }
        }
        // Custom field audiobook takes precedence over tags
        else
        if (book.getTitle().contains("(audiobook)")) {
            if (!book.isAudioBookFromTags()) {
                String title = book.getTitle().replaceAll("\\(audiobook\\)", "").trim();
                if (title.contains(":")) {
                    String[] titleParts = title.split(":");
                    title = titleParts[0].trim() + ": " + titleParts[1].trim();
                }
                System.out.printf("%-30s for \"%s\" to \"%s\"%n", "[remove audiobook from title]", book.getTitle(), title);
                if (!dryRun) {
                    calibredb.updateBookTitle(book.getId(), title);
                }
                itemUpdated = true;
            }
        }
        return itemUpdated;
    }

    boolean processReadOrder(boolean dryRun, Book book) throws SQLException {
        String readOrderFromCustomField = book.getReadOrderFromCustomField();
        String[] originalTags = book.getTags().split(",");
        List<String> tagsList = new ArrayList<>(Arrays.asList(originalTags));
        List<String> readOrderTags = Arrays.stream(originalTags)
                .filter(tag -> tag.trim().startsWith("readorder:"))
                .map(tag -> tag.trim().replace("readorder:", ""))
                .toList();
        String readOrderFromTags;
        if (!readOrderTags.isEmpty()) {
            readOrderFromTags = readOrderTags.get(0);
        } else {
            readOrderFromTags = NO_READORDER;
        }
        boolean itemUpdated = false;
        // Case 1: More than one readorder tag, we keep the first one
        if (readOrderTags.size() > 1) {
            System.out.printf("%-30s for \"%s\"%n", "[remove extra readorder tags]", book.getTitle());
            tagsList = tagsList.stream()
                    .filter(tag -> !tag.trim().startsWith("readorder:"))
                    .collect(Collectors.toList());
            tagsList.add("readorder:" + readOrderFromTags);
            itemUpdated = true;
        }
        // Case 2: No readorder tag but custom field exists, we add the tag
        if (NO_READORDER.equals(readOrderFromTags) && !NO_READORDER.equals(readOrderFromCustomField)) {
            System.out.printf("%-30s for \"%s\"%n", "[add readorder tag]", book.getTitle());
            tagsList.add("readorder:" + readOrderFromCustomField);
            itemUpdated = true;
        }
        // Case 3: Mismatch between tag and custom field, custom field wins
        else if (!NO_READORDER.equals(readOrderFromTags) && !NO_READORDER.equals(readOrderFromCustomField) &&
                 !readOrderFromTags.equals(readOrderFromCustomField)) {
            System.out.printf("%-30s for \"%s\" from \"%s\" to \"%s\"%n", "[update readorder]", book.getTitle(), readOrderFromTags, readOrderFromCustomField);
            // Update the tag to match the custom field value
            tagsList = tagsList.stream()
                    .filter(tag -> !tag.trim().startsWith("readorder:"))
                    .collect(Collectors.toList());
            tagsList.add("readorder:" + readOrderFromCustomField);
            itemUpdated = true;
        }
        // Case 4: Delete custom field if value is 0.0
        if (readOrderFromCustomField.equals("0.0")) {
            if (!dryRun) {
                calibredb.deleteReadOrderCustomField(book.getId());
            }
            System.out.printf("%-30s for \"%s\"%n", "[delete custom field readorder]", book.getTitle());
            // Skip further processing of this book
            readOrderFromCustomField = NO_READORDER;
        }
        // Case 5: Delete readorder tag if custom field is empty
        // Skip the update if there are no changes to the tags
        if (NO_READORDER.equals(readOrderFromCustomField) && !NO_READORDER.equals(readOrderFromTags)) {
            tagsList = tagsList.stream()
                    .filter(tag -> !tag.trim().startsWith("readorder:"))
                    .collect(Collectors.toList());
            System.out.printf("%-30s for \"%s\" from \"%s\" to \"%s\"%n", "[delete readorder tag]", book.getTitle(), readOrderFromTags, readOrderFromCustomField);
            itemUpdated = true;
        }

        if (!dryRun && !Arrays.stream(originalTags).sorted().toList().equals(tagsList.stream().sorted().toList())) {
            calibredb.replaceBookTags(book, tagsList);
        }

        return itemUpdated;
    }
}