package calibreautomations;

import calibreautomations.persistence.CalibreDBJdbc;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ProcessAudiobookTest {

    @Test
    void testProcessAudiobook_AddsAudiobookToTitle_with_subtitle() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreDBJdbc mockCalibreDB = mock(CalibreDBJdbc.class);
        CalibreUpdater updater = new CalibreUpdater(mockConnection, mockCalibreDB);

        Book book = new Book(1, "Test Book: the adventure", "tag1,format:audiobook,tag2", "3.0");
        boolean result = updater.processAudiobook(false, book);

        assertTrue(result);
        verify(mockCalibreDB).updateBookTitle(eq(1), eq("Test Book (audiobook): the adventure"));
    }


    @Test
    void testProcessAudiobook_AddsAudiobookToTitle_without_subtitle() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreDBJdbc mockCalibreDB = mock(CalibreDBJdbc.class);
        CalibreUpdater updater = new CalibreUpdater(mockConnection, mockCalibreDB);

        Book book = new Book(1, "Test Book", "tag1,format:audiobook,tag2", "3.0");
        boolean result = updater.processAudiobook(false, book);

        assertTrue(result);
        verify(mockCalibreDB).updateBookTitle(eq(1), eq("Test Book (audiobook)"));
    }

    @Test
    void testProcessAudiobook_RemovesAudiobookFromTitle_with_subtitle() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreDBJdbc mockCalibreDB = mock(CalibreDBJdbc.class);
        CalibreUpdater updater = new CalibreUpdater(mockConnection, mockCalibreDB);

        Book book = new Book(1, "Test Book (audiobook): the adventure", "tag1,tag2", "3.0");
        boolean result = updater.processAudiobook(false, book);

        assertTrue(result);
        verify(mockCalibreDB).updateBookTitle(eq(1), eq("Test Book: the adventure"));
    }

    @Test
    void testProcessAudiobook_RemovesAudiobookFromTitle_without_subtitle() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreDBJdbc mockCalibreDB = mock(CalibreDBJdbc.class);
        CalibreUpdater updater = new CalibreUpdater(mockConnection, mockCalibreDB);

        Book book = new Book(1, "Test Book (audiobook)", "tag1,tag2", "3.0");
        boolean result = updater.processAudiobook(false, book);

        assertTrue(result);
        verify(mockCalibreDB).updateBookTitle(eq(1), eq("Test Book"));
    }
}