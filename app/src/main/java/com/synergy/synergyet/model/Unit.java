package com.synergy.synergyet.model;

import java.util.ArrayList;
import java.util.List;

public class Unit {
    private int unit_id;
    private String name;
    private int hours;
    private List<UnitTask> tasks;
    private ArrayList<Object[]> assistance;
    private int course_id;
    private int order;

    /**
     * Para poder insertar los datos de una clase personalizada en Cloud Firestore, se necesita un constructor público sin parámetros
     */
    public Unit() {}

    public int getUnit_id() {
        return unit_id;
    }

    public String getName() {
        return name;
    }

    public int getHours() {
        return hours;
    }

    public List<UnitTask> getTasks() {
        return tasks;
    }

    public ArrayList<Object[]> getAssistance() {
        return assistance;
    }

    public int getCourse_id() {
        return course_id;
    }

    public int getOrder() {
        return order;
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

    public void setTasks(List<UnitTask> unitTasks) {
        this.tasks = unitTasks;
    }

    public void setAssistance(ArrayList<Object[]> assistance) {
        this.assistance = assistance;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    public void setOrder(int order) {
        this.order = order;
    }


}
