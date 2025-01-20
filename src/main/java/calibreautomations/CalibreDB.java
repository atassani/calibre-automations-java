package calibreautomations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CalibreDB {

    private final Connection connection;

    public CalibreDB(Connection connection) {
        this.connection = connection;
    }

    String getReadOrderTable() throws SQLException {
        String query = "SELECT id FROM custom_columns WHERE label = 'readorder'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return "custom_column_" + rs.getInt("id");
            }
        }
        return null;
    }

    List<Book> getBooks(String readOrderTable) throws SQLException {
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
        //noinspection SqlSourceToSinkFlow
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
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

    private void updateReadOrder(Book book, String newOrder, String readOrderTable) throws SQLException {
        // language=SQLite
        String updateQuery = String.format("UPDATE %s SET value = ? WHERE book = ?", readOrderTable);
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, newOrder);
            pstmt.setInt(2, book.getId());
            pstmt.executeUpdate();
        }
    }
}
