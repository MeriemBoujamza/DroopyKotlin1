package ma.ensaf.veryempty.models;

import java.io.Serializable;

public class Posts implements Serializable {
    private int id;
    private Users user;
    private String timeAgo;
    private String postContent;
    private String postImage;
    private boolean isLiked;

    public Posts() {
    }

    public Posts(int id, Users user, String timeAgo, String postContent, String postImage) {
        this.id = id;
        this.user = user;
        this.timeAgo = timeAgo;
        this.postContent = postContent;
        this.postImage = postImage;
    }

    public Posts(int id, Users user, String timeAgo, String postContent, String postImage, boolean isLiked) {
        this.id = id;
        this.user = user;
        this.timeAgo = timeAgo;
        this.postContent = postContent;
        this.postImage = postImage;
        this.isLiked = isLiked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}