package unimelb.comp90018_instaviewer.models;

public class FeedPost {
    //Insert DB post stuff here
    private String UserId;
    private String postId;
    private String ownerName;
    private String imageUrl;
    private String message;
    private int nLikes;
    private int nComments;


    public FeedPost(String UserId, String postId, String ownerName, String imageUrl, String message, int nLikes, int nComments) {
        this.UserId = UserId;
        this.postId = postId;
        this.message = message;
        this.ownerName = ownerName;
        this.imageUrl = imageUrl;
        this.nLikes = nLikes;
        this.nComments = nComments;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public int getnComments(){ return nComments; }

    public int getnLikes() {
        return nLikes;
    }

    public String getPostId() {
        return postId;
    }
}
