package calibreautomations;

import calibreautomations.persistence.CalibreDBCli;
import calibreautomations.persistence.DataAccessException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class ProcessReadOrderTest {

    @Test
    void test_more_than_one_readorder_gets_first_if_no_custom_field() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books with multiple readorder tags
        List<Book> mockBooks = List.of(
                new Book(1, "Test Book 1", "tag1,readorder:2.0,readorder:3.0,tag2", "2.0")
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        when(mockOptions.isAudiobooks()).thenReturn(false);
        when(mockOptions.isReadorders()).thenReturn(true);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-r"};
        mockUpdater.run(args);

        // Capture the arguments passed to replaceBookTags
        ArgumentCaptor<List<String>> tagsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockCalibreDB).replaceBookTags(eq(1), tagsCaptor.capture());

        // Verify that only the first readorder tag is present
        List<String> capturedTags = tagsCaptor.getValue();
        assertTrue(capturedTags.contains("readorder:2.0"));
        assertTrue(capturedTags.stream().noneMatch(tag -> tag.equals("readorder:3.0")));
    }

    @Test
    void test_if_no_readorder_tag_and_custom_field_exists_adds_readorder_tag() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books with no readorder tag
        List<Book> mockBooks = List.of(
                new Book(1, "Test Book 1", "tag1,tag2", "2.0")
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        when(mockOptions.isAudiobooks()).thenReturn(false);
        when(mockOptions.isReadorders()).thenReturn(true);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-r"};
        mockUpdater.run(args);

        // Capture the arguments passed to replaceBookTags
        ArgumentCaptor<List<String>> tagsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockCalibreDB).replaceBookTags(eq(1), tagsCaptor.capture());

        // Verify that the readorder tag is added
        List<String> capturedTags = tagsCaptor.getValue();
        assertTrue(capturedTags.contains("readorder:2.0"));
    }

    @Test
    void test_if_readorder_custom_field_different_from_tag_custom_field_wins() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books with different readorder tag and custom field
        List<Book> mockBooks = List.of(
                new Book(1, "Test Book 1", "tag1,readorder:2.0,tag2", "3.0")
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        when(mockOptions.isAudiobooks()).thenReturn(false);
        when(mockOptions.isReadorders()).thenReturn(true);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-r"};
        mockUpdater.run(args);

        // Capture the arguments passed to replaceBookTags
        ArgumentCaptor<List<String>> tagsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockCalibreDB).replaceBookTags(eq(1), tagsCaptor.capture());

        // Verify that the readorder tag is updated to match the custom field value
        List<String> capturedTags = tagsCaptor.getValue();
        assertTrue(capturedTags.contains("readorder:3.0"));
        assertTrue(capturedTags.stream().noneMatch(tag -> tag.equals("readorder:2.0")));
    }

    @Test
    void test_if_readorder_custom_field_same_as_tag_no_update() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books with same readorder tag and custom field
        List<Book> mockBooks = List.of(
                new Book(1, "Test Book 1", "tag1,readorder:2.0,tag2", "2.0")
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        when(mockOptions.isAudiobooks()).thenReturn(false);
        when(mockOptions.isReadorders()).thenReturn(true);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-r"};
        mockUpdater.run(args);

        // Verify that replaceBookTags is not called
        verify(mockCalibreDB, never()).replaceBookTags(anyInt(), any());
    }

    @Test
    void test_if_custom_field_is_0_0_removes_tag() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books with readorder tag 0.0
        List<Book> mockBooks = List.of(
                new Book(1, "Test Book 1", "tag1,readorder:2.0,tag2", "0.0")
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        when(mockOptions.isAudiobooks()).thenReturn(false);
        when(mockOptions.isReadorders()).thenReturn(true);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-r"};
        mockUpdater.run(args);

        // Capture the arguments passed to replaceBookTags
        ArgumentCaptor<List<String>> tagsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockCalibreDB).replaceBookTags(eq(1), tagsCaptor.capture());

        // Verify that calibredb.deleteReadOrderCustomField is called
        verify(mockCalibreDB).deleteReadOrderCustomField(eq(1));

        // Verify that the readorder tag is removed
        List<String> capturedTags = tagsCaptor.getValue();
        assertTrue(capturedTags.stream().noneMatch(tag -> tag.equals("readorder:2.0")));
    }

    @Test
    void test_if_readorder_custom_field_is_empty_removes_the_tag() throws DataAccessException {
        CalibreDBCli mockCalibreDB = mock(CalibreDBCli.class);
        CalibreUpdater mockUpdater = Mockito.spy(new CalibreUpdater(mockCalibreDB));
        AppOptions mockOptions = mock(AppOptions.class);

        // Mock the getBooks method to return a list of books with readorder tag 0.0
        List<Book> mockBooks = List.of(
                new Book(1, "Test Book 1", "tag1,readorder:2.0,tag2", null)
        );
        doReturn(mockBooks).when(mockCalibreDB).getBooks();

        when(mockOptions.isAudiobooks()).thenReturn(false);
        when(mockOptions.isReadorders()).thenReturn(true);

        doNothing().when(mockUpdater).updateCalibre(mockOptions);

        String[] args = {"-r"};
        mockUpdater.run(args);

        // Capture the arguments passed to replaceBookTags
        ArgumentCaptor<List<String>> tagsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockCalibreDB).replaceBookTags(eq(1), tagsCaptor.capture());

        // Verify that the readorder tag is removed
        List<String> capturedTags = tagsCaptor.getValue();
        assertTrue(capturedTags.stream().noneMatch(tag -> tag.equals("readorder:2.0")));
    }
}