package com.synergy.synergyet.model;

public class Message {
    private String message;
    private String date;
    private String hour;
    private String sender;

    public Message(String message, String date, String hour, String sender) {
        this.message = message;
        this.date = date;
        this.hour = hour;
        this.sender = sender;
    }

    /**
     * Necesario para que Firebase mapeé la clase
     */
    public Message() {}

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    public String getSender() {
        return sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
