package calibreautomations.persistence;

import calibreautomations.Book;

import java.util.List;

public class CalibreDBcli implements CalibreDB {
    @Override
    public List<Book> getBooks() throws DataAccessException {
        return null;
    }

    @Override
    public void deleteReadOrderCustomField(int bookId) throws DataAccessException {

    }

    @Override
    public void deleteAllBookTags(int bookId) throws DataAccessException {

    }

    @Override
    public Integer getTagIfExists(String tag) throws DataAccessException {
        return 0;
    }

    @Override
    public Integer insertTag(String tag) throws DataAccessException {
        return 0;
    }

    @Override
    public void insertBookTag(int bookId, int tagId) throws DataAccessException {

    }

    @Override
    public void replaceBookTags(Book book, List<String> tagsList) throws DataAccessException, DataAccessException {

    }

    @Override
    public void updateBookTitle(int bookId, String title) throws DataAccessException {

    }
}
