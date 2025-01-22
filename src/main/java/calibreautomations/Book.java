package calibreautomations;

public class Book {
    public static final String NO_READORDER = "no-readorder";
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

    public String getReadOrderFromCustomField() {
        return readOrder != null ? readOrder : NO_READORDER;
    }

    public boolean isAudioBookFromTags() {
        return tags != null && tags.contains("format:audiobook");
    }
}