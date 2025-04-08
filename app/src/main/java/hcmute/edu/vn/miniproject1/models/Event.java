package hcmute.edu.vn.miniproject1.models;

public class Event {
    private long id;
    private String date;
    private String title;
    private String description;
    private String time;
    private int status;
    private String completedDate;
    private String completedTime;

    // Constructor đầy đủ
    public Event(long id, String date, String title, String description, String time, int status, String completedDate, String completedTime) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.description = description;
        this.time = time;
        this.status = status;
        this.completedDate = completedDate != null ? completedDate : "";  // Tránh null
        this.completedTime = completedTime != null ? completedTime : "";  // Tránh null
    }


    public Event(long id, String date, String title, String description, String time, int status) {
        this(id, date, title, description, time, status, "", "");  // Gọi constructor đầy đủ, tránh null
    }


    // 🛠 Getter và Setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getCompletedDate() { return completedDate; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate != null ? completedDate : ""; }  // ✅ Tránh null

    public String getCompletedTime() { return completedTime; }
    public void setCompletedTime(String completedTime) { this.completedTime = completedTime != null ? completedTime : ""; }  // ✅ Tránh null
}
