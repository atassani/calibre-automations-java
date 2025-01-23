package calibreautomations;

import calibreautomations.persistence.CalibreDBCli;
import calibreautomations.persistence.DataAccessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ProcessAudiobookTest {

    @Test
    void testProcessAudiobook_AddsAudiobookToTitle_with_subtitle() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater updater = new CalibreUpdater(mockCalibreDB);

        Book book = new Book(1, "Test Book: the adventure", "tag1,format:audiobook,tag2", "3.0");
        boolean result = updater.processAudiobook(false, book);

        assertTrue(result);
        verify(mockCalibreDB).updateBookTitle(eq(1), eq("Test Book (audiobook): the adventure"));
    }

    @Test
    void testProcessAudiobook_AddsAudiobookToTitle_without_subtitle() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater updater = new CalibreUpdater(mockCalibreDB);

        Book book = new Book(1, "Test Book", "tag1,format:audiobook,tag2", "3.0");
        boolean result = updater.processAudiobook(false, book);

        assertTrue(result);
        verify(mockCalibreDB).updateBookTitle(eq(1), eq("Test Book (audiobook)"));
    }

    @Test
    void testProcessAudiobook_RemovesAudiobookFromTitle_with_subtitle() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater updater = new CalibreUpdater(mockCalibreDB);

        Book book = new Book(1, "Test Book (audiobook): the adventure", "tag1,tag2", "3.0");
        boolean result = updater.processAudiobook(false, book);

        assertTrue(result);
        verify(mockCalibreDB).updateBookTitle(eq(1), eq("Test Book: the adventure"));
    }

    @Test
    void testProcessAudiobook_RemovesAudiobookFromTitle_without_subtitle() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater updater = new CalibreUpdater(mockCalibreDB);

        Book book = new Book(1, "Test Book (audiobook)", "tag1,tag2", "3.0");
        boolean result = updater.processAudiobook(false, book);

        assertTrue(result);
        verify(mockCalibreDB).updateBookTitle(eq(1), eq("Test Book"));
    }
}