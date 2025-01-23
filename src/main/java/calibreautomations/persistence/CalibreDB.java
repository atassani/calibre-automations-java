package calibreautomations.persistence;

import calibreautomations.Book;

import java.util.List;

public interface CalibreDB {
    List<Book> getBooks() throws DataAccessException;

    void deleteReadOrderCustomField(int bookId) throws DataAccessException;

    void replaceBookTags(int bookId, List<String> tagsList) throws DataAccessException;

    void updateBookTitle(int bookId, String title) throws DataAccessException;
}
