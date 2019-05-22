package com.synergy.synergyet.model;

import java.util.ArrayList;

public class Task {
    private int task_id;
    private String taskName;
    private ArrayList<String> teachers_uid;
    private int unit_id;
    private String type;
    private String dead_line;

    /**
     * Para poder insertar los datos de una clase personalizada en Cloud Firestore, se necesita un constructor público sin parámetros
     */
    private Task() {}

    public Task(int task_id, String taskName, ArrayList<String> teachers_uid, int unit_id, String type, String dead_line) {
        this.task_id = task_id;
        this.taskName = taskName;
        this.teachers_uid = teachers_uid;
        this.unit_id = unit_id;
        this.type = type;
        this.dead_line = dead_line;
    }

    public int getTask_id() {
        return task_id;
    }

    public String getTaskName() {
        return taskName;
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

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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
