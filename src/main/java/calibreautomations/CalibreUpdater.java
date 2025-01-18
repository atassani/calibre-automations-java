package calibreautomations;

import com.google.gson.Gson;
import org.apache.commons.cli.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CalibreUpdater {
    private static final String DB_URL = "jdbc:sqlite:/Users/toni.tassani/CalibreLibrary/metadata.db";
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("d", "dry-run", false, "Run the updater in dry-run mode");
        options.addOption("a", "audiobook", false, "Process audiobooks");
        options.addOption(Option.builder("r")
                .longOpt("readorder")
                .desc("Process read order")
                .build());
        options.addOption(Option.builder()
                .longOpt("readOrder")
                .desc("Process read order")
                .build());
        options.addOption(Option.builder()
                .longOpt("read-order")
                .desc("Process read order")
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("CalibreUpdater", options);
            return;
        }

        boolean dryRun = cmd.hasOption("d");

        // If no specific flag is set, process everything
        boolean processReadOrder = cmd.hasOption("r") || cmd.hasOption("readOrder") || cmd.hasOption("read-order") || !cmd.hasOption("a") || !cmd.hasOption("audiobook");
        boolean processAudiobooks = cmd.hasOption("a") || !(cmd.hasOption("r") || cmd.hasOption("readOrder") || cmd.hasOption("read-order"));

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String readOrderTable = getReadOrderTable(conn);
            if (processReadOrder && readOrderTable == null) {
                System.out.println("ERROR: Custom field 'readorder' not found in the Calibre database.");
                return;
            }
            List<Book> books = getBooks(conn, readOrderTable);
            int updatedBooks = 0;
            for (Book book : books) {
                // TODO I would like to test that processing audiobooks or readorder are called
                // TODO Validate that there is only one format (or more than one?), Validate that there is one
                // TODO Check if a finished book has a score
                if (processAudiobooks) {
                    // TODO Consider dry-run for Audiobook
                    // TODO count updated books, and updated books for audiobook or for readorder
                    if (book.isAudioBookFromTags()) {
                        if (!book.getTitle().contains("(audiobook)")) {
                            System.out.printf("%-30s for \"%s\" to ...%n", "[add audiobook to title]", book.getTitle());
                        }
                    }
                    if (book.getTitle().contains("(audiobook)")) {
                        if (!book.isAudioBookFromTags()) {
                            System.out.printf("%-30s for \"%s\" to ...%n", "[remove audiobook from title]", book.getTitle());
                        }
                    }
                }
                if (processReadOrder) {
                    String readOrderFromTags = book.getReadOrderFromTags();
                    String readOrderFromCustomField = book.getReadOrderFromCustomField();
                    if (!readOrderFromTags.equals(readOrderFromCustomField)) {
                        if (!dryRun) {
                            // TODO Update the database will require delete fields, as in the python code
                            //updateReadOrder(conn, book, readOrderFromCustomField, readOrderTable);
                        }
                        System.out.printf("%-30s for \"%s\" from \"%s\" to \"%s\"%n", "[update readorder]", book.getTitle(), readOrderFromTags, readOrderFromCustomField);
                    }
                }
            }
            String updateMessage = dryRun ? "to update (dry-run)" : "updated";
            System.out.printf("%d books %s%n", updatedBooks, updateMessage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getReadOrderTable(Connection conn) throws SQLException {
        String query = "SELECT id FROM custom_columns WHERE label = 'readorder'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return "custom_column_" + rs.getInt("id");
            }
        }
        return null;
    }

    private static List<Book> getBooks(Connection conn, String readOrderTable) throws SQLException {
        List<Book> books = new ArrayList<>();
        // language=SQLite
        String query = String.format("""
                SELECT b.id, b.title, c.value AS readorder,
                       (SELECT GROUP_CONCAT(t.name, ', ')
                        FROM books_tags_link bt
                        JOIN tags t ON bt.tag = t.id
                        WHERE bt.book = b.id) AS tags
                FROM books b
                LEFT JOIN %s c ON b.id = c.book
                ORDER BY title
                """, readOrderTable);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String tags = rs.getString("tags");
                String readOrder = rs.getString("readorder");
                books.add(new Book(id, title, tags, readOrder));
            }
        }
        return books;
    }

    private static void updateReadOrder(Connection conn, Book book, String newOrder, String readOrderTable) throws SQLException {
        // language=SQLite
        String updateQuery = String.format("UPDATE %s SET value = ? WHERE book = ?", readOrderTable);
        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, newOrder);
            pstmt.setInt(2, book.getId());
            pstmt.executeUpdate();
        }
    }
}