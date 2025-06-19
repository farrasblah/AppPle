package com.example.apel_ppb;

public class Article {
    private String title;
    private String category;
    private String url;
    private int imageResId; // Add image resource ID

    public Article(String title, String category, String url, int imageResId) {
        this.title = title;
        this.category = category;
        this.url = url;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }

    public int getImageResId() {
        return imageResId;
    }
}