package calibreautomations.persistence;

import calibreautomations.Book;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CalibreDBcli implements CalibreDB {

    @Override
    public List<Book> getBooks() throws DataAccessException {
        List<Book> books = new ArrayList<>();
        try {
            String[] command = {"calibredb", "/Users/toni.tassani/CalibreLibraryTest", "list", "--for-machine", "--fields", "title,tags,*readorder"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder jsonOutput = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new DataAccessException("Error executing calibredb command: " + errorOutput.toString());
            }

            JsonArray jsonArray = JsonParser.parseString(jsonOutput.toString()).getAsJsonArray();
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
            throw new DataAccessException("Error retrieving books", e);
        }
        return books;
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
