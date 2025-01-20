package calibreautomations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

class CalibreUpdaterTest {

    @Test
    void testGetFirstReadOrderFromTags() {
        Book book = new Book(1, "Test Book", "tag1,readorder:2.0,tag2", "3.0");
        assertEquals("2.0", book.getFirstReadOrderFromTags());
    }

    @Test
    void testGetFirstReadOrdersFromTagsWithMultipleItems() {
        Book book = new Book(1, "Test Book", "tag1, readorder:3.0, readorder:2.0, tag2, readorder:5.1", "3.0");
        assertEquals("3.0", book.getFirstReadOrderFromTags());
    }

    @Test
    void testNumReadOrdersFromTags() {
        Book book = new Book(1, "Test Book", "tag1, readorder:3.0, readorder:2.0, tag2, readorder:5.1", "3.0");
        assertEquals(3, book.getNumReadOrdersFromTags());

        book = new Book(1, "Test Book", "tag1, readorder:3.0, tag2", "3.0");
        assertEquals(1, book.getNumReadOrdersFromTags());
    }

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
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockConnection));
        AppOptions mockOptions = mock(AppOptions.class);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-a"};
        mockUpdater.run(args);

        when(mockOptions.isAudiobooks()).thenReturn(true);
        when(mockOptions.isReadorders()).thenReturn(false);
        when(mockOptions.isDryRun()).thenReturn(false);
        verify(mockUpdater).updateCalibre(mockOptions);
    }

    @Test
    void test_option_r_processes_Readorder() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockConnection));
        AppOptions mockOptions = mock(AppOptions.class);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-r"};
        mockUpdater.run(args);

        when(mockOptions.isAudiobooks()).thenReturn(false);
        when(mockOptions.isReadorders()).thenReturn(true);
        when(mockOptions.isDryRun()).thenReturn(false);


        verify(mockUpdater).updateCalibre(mockOptions);
    }


    @Test
    void test_no_option_processes_audiobook_and_readorder() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockConnection));
        AppOptions mockOptions = mock(AppOptions.class);

        when(mockOptions.isAudiobooks()).thenReturn(true);
        when(mockOptions.isReadorders()).thenReturn(true);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {};
        mockUpdater.run(args);

        verify(mockUpdater).updateCalibre(mockOptions);
    }
}