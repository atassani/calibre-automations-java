package calibreautomations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}