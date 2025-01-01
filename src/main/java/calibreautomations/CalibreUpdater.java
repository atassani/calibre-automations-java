package calibreautomations;

import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CalibreUpdater {
    private static final String DB_URL = "jdbc:sqlite:/Users/toni.tassani/CalibreLibrary/metadata.db";
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        boolean dryRun = args.length > 0 && "--dry-run".equals(args[0]);
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String readOrderTable = getReadOrderTable(conn);
            if (readOrderTable == null) {
                System.out.println("Custom field 'readorder' not found in the Calibre database.");
                return;
            }
            List<Book> books = getBooks(conn, readOrderTable);
            int updatedBooks = 0;
            for (Book book : books) {
                String readOrderFromTags = book.getReadOrderFromTags();
                String readOrderFromCustomField = book.getReadOrderFromCustomField();
                if (!readOrderFromTags.equals(readOrderFromCustomField)) {
                    if (!dryRun) {
                        // TODO Update the database will require delete fields, as in the python code
                        //updateReadOrder(conn, book, readOrderFromCustomField, readOrderTable);
                    }
                    System.out.printf("Update readorder for book \"%s\" from \"%s\" to \"%s\"%n", book.getTitle(), readOrderFromTags, readOrderFromCustomField);
                }
            }
            System.out.printf("%d books updated%n", updatedBooks);
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