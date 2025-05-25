// Course.java
package com.example.myapp;

import java.io.Serializable;

public class Course implements Serializable {
    public String courseId;
    public String title;
    public String groupId;
    public String siteId;
    public String room;
    public String startTime;
    public String endTime;
    public String dayOfWeek;
    public String professorId;

    public Course() {
        // Default constructor required for calls to DataSnapshot.getValue(Course.class)
    }

    public Course(String courseId, String title, String groupId, String siteId,
                  String room, String startTime, String endTime, String dayOfWeek,
                  String professorId) {
        this.courseId = courseId;
        this.title = title;
        this.groupId = groupId;
        this.siteId = siteId;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
        this.professorId = professorId;
    }

    // Getters and setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getSiteId() { return siteId; }
    public void setSiteId(String siteId) { this.siteId = siteId; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }
}