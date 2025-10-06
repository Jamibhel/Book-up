package com.example.bookup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Subject implements Serializable {
    private String department;
    private String courseCode;
    private String courseName;
    private String topics;
    private String role;

    public Subject(){

    }

    public Subject(String department, String courseCode, String courseName, String topics, String role){
        this.department = department;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.topics = topics;
        this.role = role;
    }


    public String getDepartment() {
        return department;
    }

    public String getCourseCode() {
        return courseCode;
    }
    public String getCourseName(){
        return courseName;
    }

    public String getTopics() {
        return topics;
    }

    public String getRole() {
        return role;
    }


    public void setDepartment(String department) {
        this.department = department;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map =  new HashMap<>();
        map.put("department", department);
        map.put("courseCode", courseCode);
        map.put("courseName", courseName);
        map.put("topics", topics);
        map.put("role", role);
        return map;
    }
    public String toString(){
        return "Subject{" +
                "department='" + department + '\'' +
                "courseCode='" + courseCode + '\'' +
                "courseName='" + courseName + '\'' +
                "topics='" + topics + '\'' +
                "role='" + role + '\'' +
                '}';
    }
}
