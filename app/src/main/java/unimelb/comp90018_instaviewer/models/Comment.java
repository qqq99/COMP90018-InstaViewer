package unimelb.comp90018_instaviewer.models;

public class Comment {
    private String message;
    private String username;
    private String timestamp;
    private String userId;

    public Comment(String message, String username, String timestamp, String userId) {
        this.message = message;
        this.username = username;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

}
