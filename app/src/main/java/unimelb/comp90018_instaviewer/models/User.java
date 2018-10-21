package unimelb.comp90018_instaviewer.models;

import java.util.HashMap;

public class User {
    private String name;
    private String avatar;
    private String userId;
    private Boolean follow;
    private int mutual;

    public User(String name, String avatar, String userId, Boolean follow) {
        this.name = name;
        this.avatar = avatar;
        this.userId = userId;
        this.follow = follow;
    }

    public User(HashMap data) {
        this.name = (String) (data.get("name") != null ? data.get("name") : "");
        this.avatar = (String) (data.get("avatar") != null ? data.get("avatar") : "");
        this.userId = (String) (data.get("userId") != null ? data.get("userId") : "");
        this.follow = (Boolean) (data.get("follow") != null ? data.get("follow") : false);
        this.mutual = (int) (data.get("mutual") != null ? data.get("mutual") : -1);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean isFollowed() {
        return follow;
    }

    public void setFollow(Boolean follow) {
        this.follow = follow;
    }

    public int getMutual() {
        return mutual;
    }

    public void setMutual(int mutual) {
        this.mutual = mutual;
    }
}
