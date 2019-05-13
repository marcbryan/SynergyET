package com.synergy.synergyet.model;

import java.util.ArrayList;

public class Task {
    private int task_id;
    private ArrayList<String> teachers_uid;
    private int unit_id;
    private String type;
    private String dead_line;

    /**
     * Para poder insertar los datos de una clase personalizada en Cloud Firestore, se necesita un constructor público sin parámetros
     */
    private Task() {}

    public int getTask_id() {
        return task_id;
    }

    public ArrayList<String> getTeachers_uid() {
        return teachers_uid;
    }

    public int getUnit_id() {
        return unit_id;
    }

    public String getType() {
        return type;
    }

    public String getDead_line() {
        return dead_line;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public void setTeachers_uid(ArrayList<String> teachers_uid) {
        this.teachers_uid = teachers_uid;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDead_line(String dead_line) {
        this.dead_line = dead_line;
    }
}
