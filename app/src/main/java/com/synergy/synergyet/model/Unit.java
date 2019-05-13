package com.synergy.synergyet.model;

import java.util.ArrayList;

public class Unit {
    private int unit_id;
    private String name;
    private int hours;
    private ArrayList<Task> tasks;
    private ArrayList<Object[]> assistance;
    private int course_id;

    /**
     * Para poder insertar los datos de una clase personalizada en Cloud Firestore, se necesita un constructor público sin parámetros
     */
    public Unit() {}

    public Unit(int unit_id, String name, int hours, int course_id) {
        this.unit_id = unit_id;
        this.name = name;
        this.hours = hours;
        this.course_id = course_id;
    }

    public int getUnit_id() {
        return unit_id;
    }

    public String getName() {
        return name;
    }

    public int getHours() {
        return hours;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public ArrayList<Object[]> getAssistance() {
        return assistance;
    }

    public int getCourse_id() {
        return course_id;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public void setAssistance(ArrayList<Object[]> assistance) {
        this.assistance = assistance;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }
}
