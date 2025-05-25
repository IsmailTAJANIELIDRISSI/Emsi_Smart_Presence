package com.example.myapp;

public class MakeupSession {
    private String sessionId;
    private String courseId;
    private String courseTitle;
    private String groupId;
    private String siteId;
    private String date;
    private String time;
    private String room;
    private String professorId;
    private String reason;

    // Default constructor required for Firestore
    public MakeupSession() {}

    public MakeupSession(String sessionId, String courseId, String courseTitle, String groupId,
                         String siteId, String date, String time, String room, String professorId, String reason) {
        this.sessionId = sessionId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.groupId = groupId;
        this.siteId = siteId;
        this.date = date;
        this.time = time;
        this.room = room;
        this.professorId = professorId;
        this.reason = reason;
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getRoom() {
        return room;
    }

    public String getProfessorId() {
        return professorId;
    }

    public String getReason() {
        return reason;
    }

    // Setters
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}