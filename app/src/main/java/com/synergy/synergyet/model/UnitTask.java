package com.synergy.synergyet.model;

import java.io.Serializable;

// Debe implementar la interfaz Serializable para poder pasar este objeto de un activity a otro con el método putExtra()
public class UnitTask implements Serializable {
    private int task_id;
    private String taskName;
    private int unit_id;
    private String type;
    private String dead_line;
    private String fileURL;

    /**
     * Para poder insertar los datos de una clase personalizada en Cloud Firestore, se necesita un constructor público sin parámetros
     */
    private UnitTask() {}

    public UnitTask(int task_id, String taskName, int unit_id, String type, String dead_line) {
        this.task_id = task_id;
        this.taskName = taskName;
        this.unit_id = unit_id;
        this.type = type;
        this.dead_line = dead_line;
    }

    public UnitTask(String taskName, int unit_id, String type, String dead_line) {
        this.taskName = taskName;
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

    public int getUnit_id() {
        return unit_id;
    }

    public String getType() {
        return type;
    }

    public String getDead_line() {
        return dead_line;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }
}
