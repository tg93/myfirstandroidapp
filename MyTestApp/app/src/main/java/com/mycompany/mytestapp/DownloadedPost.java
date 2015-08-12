package com.mycompany.mytestapp;

/**
 * Created by Tomek on 2015-07-30.
 */
public class DownloadedPost {
    private String id;
    private String userId;
    private String title;
    private String body;

    public DownloadedPost() {
    }

    public DownloadedPost(String id, String userId, String title, String body) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.body = body;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return id+"\n"+userId+"\n"+title+"\n"+body;
    }
}
