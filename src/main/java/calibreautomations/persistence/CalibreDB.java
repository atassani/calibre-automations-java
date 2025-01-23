package calibreautomations.persistence;

import calibreautomations.Book;

import java.util.List;

public interface CalibreDB {
    List<Book> getBooks() throws DataAccessException;

    void deleteReadOrderCustomField(int bookId) throws DataAccessException;

    void deleteAllBookTags(int bookId) throws DataAccessException;

    Integer getTagIfExists(String tag) throws DataAccessException;

    Integer insertTag(String tag) throws DataAccessException;

    void insertBookTag(int bookId, int tagId) throws DataAccessException;

    void replaceBookTags(Book book, List<String> tagsList) throws DataAccessException, DataAccessException;

    void updateBookTitle(int bookId, String title) throws DataAccessException;
}
