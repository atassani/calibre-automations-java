package calibreautomations.persistence;

import calibreautomations.Book;

import java.sql.SQLException;
import java.util.List;

public interface CalibreDB {
    List<Book> getBooks() throws SQLException;

    void deleteReadOrderCustomField(int bookId) throws SQLException;

    void deleteAllBookTags(int bookId) throws SQLException;

    Integer getTagIfExists(String tag) throws SQLException;

    Integer insertTag(String tag) throws SQLException;

    void insertBookTag(int bookId, int tagId) throws SQLException;

    void replaceBookTags(Book book, List<String> tagsList) throws SQLException;

    void updateBookTitle(int bookId, String title) throws SQLException;
}
