package calibreautomations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

class CalibreUpdaterTest {

    @Test
    void testGetReadOrderFromTags() {
        Book book = new Book(1, "Test Book", "tag1,readorder:2.0,tag2", "3.0");
        assertEquals("2.0", book.getReadOrderFromTags());
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
    void testMainWithAudiobookOption() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockConnection));

        doNothing().when(mockUpdater).updateCalibre(any(Connection.class), anyBoolean(), anyBoolean(), anyBoolean());

        String[] args = {"-a"};
        mockUpdater.run(args);

        verify(mockUpdater).updateCalibre(eq(mockConnection), anyBoolean(), eq(true), anyBoolean());
    }
}