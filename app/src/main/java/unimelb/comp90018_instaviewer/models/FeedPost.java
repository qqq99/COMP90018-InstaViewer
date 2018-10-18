package unimelb.comp90018_instaviewer.models;

public class FeedPost {
    //Insert DB post stuff here
    private String mUserId;
    private String mOwnerName;
    private String mImageUrl;
    private String mMessage;
    private int mLikes;
    private String[] mComments;


    public FeedPost(String mUserId, String mImageUrl, String mMessage, int mLikes, String[] mComments) {
        this.mUserId = mUserId;
        this.mMessage = mMessage;
        this.mOwnerName = mOwnerName;
        this.mImageUrl = mImageUrl;
        this.mLikes = mLikes;
        this.mComments = mComments;
    }

    public String getmOwnerName() {
        return mOwnerName;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public String getmMessage() {
        return mMessage;
    }
}
