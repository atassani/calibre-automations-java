package calibreautomations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

class CalibreUpdaterTest {

    @Test
    void testGetReadOrderFromCustomField() {
        Book book = new Book(1, "Test Book", "tag1,readorder:2.0,tag2", "3.0");
        assertEquals("3.0", book.getReadOrderFromCustomField());
    }

    @Test
    void testGetAudiobookFromTags() {
        Book book = new Book(1, "Test Book", "tag1,format:audiobook,tag2", "3.0");
        assertTrue(book.isAudioBookFromTags());
    }

    @Test
    void testGetNotAudiobookFromTags() {
        Book book = new Book(1, "Test Book", "tag1,tag2", "3.0");
        assertFalse(book.isAudioBookFromTags());
    }

    @Test
    void test_option_a_processes_Audiobook() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreDB mockCalibreDB = mock(CalibreDB.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockConnection, mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books
        List<Book> mockBooks = Arrays.asList(
                new Book(1, "Test Book 1", "tag1,readorder:2.0,tag2", "3.0"),
                new Book(2, "Test Book 2", "tag1,format:audiobook,tag2", "3.0")
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-a"};
        mockUpdater.run(args);

        when(mockOptions.isAudiobooks()).thenReturn(true);
        when(mockOptions.isReadorders()).thenReturn(false);
        when(mockOptions.isDryRun()).thenReturn(false);

        // Verify that processAudiobook and processReadOrder are called
        for (Book book : mockBooks) {
            verify(mockUpdater).processAudiobook(anyBoolean(), eq(book));
            verify(mockUpdater, never()).processReadOrder(anyBoolean(), eq(book));
        }
    }

    @Test
    void test_option_r_processes_Readorder() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreDB mockCalibreDB = mock(CalibreDB.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockConnection, mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books
        List<Book> mockBooks = Arrays.asList(
                new Book(1, "Test Book 1", "tag1,readorder:2.0,tag2", "3.0"),
                new Book(2, "Test Book 2", "tag1,format:audiobook,tag2", "3.0")
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-r"};
        mockUpdater.run(args);

        when(mockOptions.isAudiobooks()).thenReturn(false);
        when(mockOptions.isReadorders()).thenReturn(true);
        when(mockOptions.isDryRun()).thenReturn(false);

        // Verify that processAudiobook and processReadOrder are called
        for (Book book : mockBooks) {
            verify(mockUpdater, never()).processAudiobook(anyBoolean(), eq(book));
            verify(mockUpdater).processReadOrder(anyBoolean(), eq(book));
        }
    }


    @Test
    void test_no_option_processes_audiobook_and_readorder() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreDB mockCalibreDB = mock(CalibreDB.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockConnection, mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books
        List<Book> mockBooks = Arrays.asList(
                new Book(1, "Test Book 1", "tag1,readorder:2.0,tag2", "3.0"),
                new Book(2, "Test Book 2", "tag1,format:audiobook,tag2", "3.0")
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        when(mockOptions.isAudiobooks()).thenReturn(true);
        when(mockOptions.isReadorders()).thenReturn(true);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {};
        mockUpdater.run(args);

        // Verify that processAudiobook and processReadOrder are called
        for (Book book : mockBooks) {
            verify(mockUpdater).processAudiobook(anyBoolean(), eq(book));
            verify(mockUpdater).processReadOrder(anyBoolean(), eq(book));
        }
    }

    // TODO Tests for the 5 cases of readorder with dryrun and without
}