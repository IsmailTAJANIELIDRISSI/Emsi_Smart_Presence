// DaySchedule.java
package com.example.myapp;

import java.util.Date;
import java.util.List;

public class DaySchedule {
    public String dayName;
    public Date date;
    public List<Course> courses;
    public boolean isToday;

    public DaySchedule() {
        // Default constructor
    }

    public DaySchedule(String dayName, Date date, List<Course> courses, boolean isToday) {
        this.dayName = dayName;
        this.date = date;
        this.courses = courses;
        this.isToday = isToday;
    }
}