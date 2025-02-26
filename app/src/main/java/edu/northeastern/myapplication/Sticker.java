package edu.northeastern.myapplication;

public class Sticker {
    private String id;
    private String name;
    private int resourceId;

    public Sticker() {
        // Required empty constructor for Firebase
    }

    public Sticker(String id, String name, int resourceId) {
        this.id = id;
        this.name = name;
        this.resourceId = resourceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}