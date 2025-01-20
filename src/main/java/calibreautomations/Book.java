package calibreautomations;

import java.util.Arrays;

public class Book {
    private final int id;
    private final String title;
    private final String tags;
    private final String readOrder;

    public Book(int id, String title, String tags, String readOrder) {
        this.id = id;
        this.title = title;
        this.tags = tags;
        this.readOrder = readOrder;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTags() {
        return tags;
    }

    public String getFirstReadOrderFromTags() {
        return tags != null && tags.contains("readorder:") ? tags.split("readorder:")[1].split(",")[0] : "no-readorder";
    }

    public int getNumReadOrdersFromTags() {
        return tags == null || !tags.contains("readorder:") ? 0 : (int) Arrays.stream(tags.split(",")).filter(s -> s.trim().startsWith("readorder:")).count();
    }

    public String getReadOrderFromCustomField() {
        return readOrder != null ? readOrder : "no-readorder";
    }

    public boolean isAudioBookFromTags() {
        return tags != null && tags.contains("format:audiobook");
    }
}