package com.synergy.synergyet.model;

public class TaskDelivered {
    private int deliver_id;
    private String student_uid;
    private int course_id;
    private int unit_id;
    private int task_id;
    private double grade;
    private String type;
    private String date_delivered;
    private String url_file_delivered;
    private String file_name;
    private String delivered_by;

    /**
     * Constructor vacío, necesario para Cloud Firestore
     */
    public TaskDelivered() {}

    public TaskDelivered(String student_uid, int course_id, int unit_id, int task_id, double grade, String type, String date_delivered, String file_name, String delivered_by) {
        this.student_uid = student_uid;
        this.course_id = course_id;
        this.unit_id = unit_id;
        this.task_id = task_id;
        this.grade = grade;
        this.type = type;
        this.date_delivered = date_delivered;
        this.file_name = file_name;
        this.delivered_by = delivered_by;
    }

    public int getDeliver_id() {
        return deliver_id;
    }

    public String getStudent_uid() {
        return student_uid;
    }

    public int getCourse_id() {
        return course_id;
    }

    public int getUnit_id() {
        return unit_id;
    }

    public int getTask_id() {
        return task_id;
    }

    public double getGrade() {
        return grade;
    }

    public String getType() {
        return type;
    }

    public String getDate_delivered() {
        return date_delivered;
    }

    public String getUrl_file_delivered() {
        return url_file_delivered;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getDelivered_by() {
        return delivered_by;
    }

    public void setDeliver_id(int deliver_id) {
        this.deliver_id = deliver_id;
    }

    public void setStudent_uid(String student_uid) {
        this.student_uid = student_uid;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDate_delivered(String date_delivered) {
        this.date_delivered = date_delivered;
    }

    public void setUrl_file_delivered(String url_file_delivered) {
        this.url_file_delivered = url_file_delivered;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public void setDelivered_by(String delivered_by) {
        this.delivered_by = delivered_by;
    }
}
