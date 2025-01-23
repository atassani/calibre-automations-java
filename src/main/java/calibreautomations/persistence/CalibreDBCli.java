package calibreautomations.persistence;

import calibreautomations.Book;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CalibreDBCli implements CalibreDB {

    private static final Logger logger = LoggerFactory.getLogger(CalibreDBCli.class);

    private final String calibreLibraryPath;

    public CalibreDBCli(String calibreLibraryPath) {
        this.calibreLibraryPath = calibreLibraryPath;
    }

    @Override
    public List<Book> getBooks() throws DataAccessException {
        List<Book> books = new ArrayList<>();
        try {
            String[] command = {"calibredb", "list", "--for-machine", "--fields", "title,tags,*readorder",  "--library-path=" + calibreLibraryPath};
            String jsonOutput = executeCalibreCommand(command);

            JsonArray jsonArray = JsonParser.parseString(jsonOutput).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                int id = jsonObject.get("id").getAsInt();
                String title = jsonObject.get("title").getAsString();
                String readOrder = jsonObject.has("*readorder") ? jsonObject.get("*readorder").getAsString() : null;
                JsonArray tagsArray = jsonObject.get("tags").getAsJsonArray();
                StringBuilder tagsBuilder = new StringBuilder();
                for (int j = 0; j < tagsArray.size(); j++) {
                    if (j > 0) {
                        tagsBuilder.append(", ");
                    }
                    tagsBuilder.append(tagsArray.get(j).getAsString());
                }
                String tags = tagsBuilder.toString();
                books.add(new Book(id, title, tags, readOrder));
            }
        } catch (Exception e) {
            throw new DataAccessException("Error retrieving books.\n" + e.getMessage());
        }
        return books;
    }

    @Override
    public void deleteReadOrderCustomField(int bookId) throws DataAccessException {
        // invoke calibredb to remove the custom field readorder
        try {
            String[] command = {"calibredb", "set_custom", "readorder", String.valueOf(bookId), "", "--library-path=" + calibreLibraryPath};
            executeCalibreCommand(command);
        } catch (Exception e) {
            throw new DataAccessException("Error deleting readorder custom field", e);
        }
    }

    @Override
    public void replaceBookTags(int bookId, List<String> tagsList) throws DataAccessException {
        try {
            String tags = String.join(",", tagsList);
            String[] command = {"calibredb", "set_metadata", String.valueOf(bookId), "--field", "tags:" + tags, "--library-path=" + calibreLibraryPath};
            executeCalibreCommand(command);
        } catch (Exception e) {
            throw new DataAccessException("Error replacing book tags", e);
        }
    }

    @Override
    public void updateBookTitle(int bookId, String title) throws DataAccessException {
        // invoke calibredb to change the book tittle
        try {
            String[] command = {"calibredb", "set_metadata", String.valueOf(bookId), "--field", "title:\"" + title + "\"", "--library-path=" + calibreLibraryPath};
            executeCalibreCommand(command);
        } catch (Exception e) {
            throw new DataAccessException("Error updating book title", e);
        }
    }

    private static String executeCalibreCommand(String[] command) throws IOException, InterruptedException, DataAccessException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        BufferedReader standardReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder standardOutput = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        String line;

        while ((line = standardReader.readLine()) != null) {
            standardOutput.append(line);
        }
        logger.debug("Output for command: {} \n {}", String.join(" ", command), standardOutput);

        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line);
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new DataAccessException(errorOutput.toString());
        }

        return standardOutput.toString();
    }

}
