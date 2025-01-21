package calibreautomations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CalibreDB {

    private final Connection connection;
    private String readOrderTable;

    public CalibreDB(Connection connection) throws SQLException {
        this.connection = connection;
        String query = "SELECT id FROM custom_columns WHERE label = 'readorder'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                this.readOrderTable = "custom_column_" + rs.getInt("id");
            } else {
                throw new IllegalStateException();
            }
        }
    }

    List<Book> getBooks() throws SQLException {
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
                """, this.readOrderTable);
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

    private void updateReadOrder(Book book, String newOrder) throws SQLException {
        // language=SQLite
        String updateQuery = String.format("UPDATE %s SET value = ? WHERE book = ?", this.readOrderTable);
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, newOrder);
            pstmt.setInt(2, book.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteReadOrderCustomField(int bookId) throws SQLException {
        // language=SQLite
        String deleteQuery = String.format("DELETE FROM %s WHERE book = ?", this.readOrderTable);
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
        }
    }

    public void deleteAllBookTags(int bookId) throws SQLException {
        // language=SQLite
        String deleteQuery = "DELETE FROM books_tags_link WHERE book = ?";
        PreparedStatement pstmt = connection.prepareStatement(deleteQuery);
        pstmt.setInt(1, bookId);
        pstmt.executeUpdate();
    }

    public Integer getTagIfExists(String tag) throws SQLException {
        // language=SQLite
        String query = "SELECT id FROM tags WHERE name = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, tag);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getInt("id") : null;
        }
    }

    public Integer insertTag(String tag) throws SQLException {
        // language=SQLite
        String insertQuery = "INSERT INTO tags (name) VALUES (?)";
        PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, tag);
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();
        return rs.next() ? rs.getInt(1) : null;
    }

    public void insertBookTag(int bookId, int tagId) throws SQLException {
        // language=SQLite
        String insertQuery = "INSERT INTO books_tags_link (book, tag) VALUES (?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(insertQuery);
        pstmt.setInt(1, bookId);
        pstmt.setInt(2, tagId);
        pstmt.executeUpdate();
    }

    void replaceBookTags(Book book, List<String> tagsList) throws SQLException {
        deleteAllBookTags(book.getId());
        for (String tag: tagsList) {
            Integer tagId = getTagIfExists(tag);
            if (tagId == null) {
                tagId = insertTag(tag);
            }
            insertBookTag(book.getId(), tagId);
        }
    }
}
