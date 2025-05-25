package com.example.myapp;

/**
 * Model class for Student data
 */
public class Student {
    private String id;
    private String name;
    private String groupId;
    private String siteId;
    private boolean present;
    private String departureTime;

    // Required empty constructor for Firestore
    public Student() {
    }

    public Student(String id, String name, String groupId, String siteId) {
        this.id = id;
        this.name = name;
        this.groupId = groupId;
        this.siteId = siteId;
        this.present = true; // Default to present
        this.departureTime = null;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
}