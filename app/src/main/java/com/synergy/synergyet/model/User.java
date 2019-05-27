package com.synergy.synergyet.model;

import java.io.Serializable;
import java.util.ArrayList;

// Debe implementar la interfaz Serializable para poder pasar este objeto de un activity a otro con el método putExtra()
public class User implements Serializable {
    private String UID;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String type;
    private ArrayList<Integer> courses;

    /**
     * Para poder insertar los datos de una clase personalizada en Cloud Firestore, se necesita un constructor público sin parámetros
     */
    public User(){

    }

    public User(String name, String surname, String email, String type) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.type = type;
    }

    public String getUID() {
        return UID;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Integer> getCourses() {
        return courses;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String username) {
        this.email = username;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCourses(ArrayList<Integer> courses) {
        this.courses = courses;
    }
}
