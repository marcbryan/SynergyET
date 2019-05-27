package com.synergy.synergyet.model;

import java.io.Serializable;
import java.util.ArrayList;

// Debe implementar la interfaz Serializable para poder pasar este objeto de un activity a otro con el método putExtra()
public class Course implements Serializable {
    private int course_id;
    private String name;
    private String category;
    private String level;
    private int hours;
    private String password;
    private ArrayList<String> teachers;
    private ArrayList<String> students;
    private String course_year;
    private boolean finished;

    /**
     * Para poder insertar los datos de una clase personalizada en Cloud Firestore, se necesita un constructor público sin parámetros
     */
    public Course(){}

    public Course(int course_id, String name, String category, String level, int hours, String password, String course_year, boolean finished) {
        this.course_id = course_id;
        this.name = name;
        this.category = category;
        this.level = level;
        this.hours = hours;
        this.password = password;
        this.course_year = course_year;
        this.finished = finished;
    }

    public int getCourse_id() {
        return course_id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getLevel() {
        return level;
    }

    public int getHours() {
        return hours;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getTeachers() {
        return teachers;
    }

    public ArrayList<String> getStudents() {
        return students;
    }

    public String getCourse_year() {
        return course_year;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTeachers(ArrayList<String> teachers) {
        this.teachers = teachers;
    }

    public void setStudents(ArrayList<String> students) {
        this.students = students;
    }

    public void setCourse_year(String course_year) {
        this.course_year = course_year;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
