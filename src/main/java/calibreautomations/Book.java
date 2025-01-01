package calibreautomations;

public class Book {
    private int id;
    private String title;
    private String tags;
    private String readOrder;

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

    public String getReadOrderFromTags() {
        return tags != null && tags.contains("readorder:") ? tags.split("readorder:")[1].split(",")[0] : "no-readorder";
    }

    public String getReadOrderFromCustomField() {
        return readOrder != null ? readOrder : "no-readorder";
    }
}