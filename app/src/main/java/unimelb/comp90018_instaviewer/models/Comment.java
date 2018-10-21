package unimelb.comp90018_instaviewer.models;

import javax.annotation.Nullable;

public class Comment {
    private String message;
    private String username;
    private String timestamp;
    private String userId;
    private String avatar;


    public Comment(String message, String userId, String timestamp, String username, @Nullable String avatar) {
        this.message = message;
        this.username = username;
        this.timestamp = timestamp;
        this.userId = userId;
        this.avatar = avatar;
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

    public String getAvatar() {
        return avatar;
    }

}
