package unimelb.comp90018_instaviewer.models;

import java.util.HashMap;

public class ProfilePost {
    private String postId;
    private String medialink;

    public ProfilePost(String postId, String mediaLink) {
        this.postId = postId;
        this.medialink = mediaLink;
    }

    public ProfilePost(HashMap post) {
        this.postId = (String) post.get("id");
        this.medialink = (String) post.get("mediaLink");
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getMedialink() {
        return medialink;
    }

    public void setMedialink(String medialink) {
        this.medialink = medialink;
    }
}
