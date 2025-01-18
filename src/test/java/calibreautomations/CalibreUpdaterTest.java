package calibreautomations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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


}