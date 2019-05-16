package com.synergy.synergyet.model;

import java.io.Serializable;
import java.util.ArrayList;

// Debe implementar la interfaz Serializable para poder pasar este objeto de un activity a otro con el método putExtra()
public class User implements Serializable {
    private String UID;
    private String name;
    private String surname;
    private String username;
    private String password;
    private String type;
    private ArrayList<Integer> courses;

    /**
     * Para poder insertar los datos de una clase personalizada en Cloud Firestore, se necesita un constructor público sin parámetros
     */
    public User(){

    }

    public User(String name, String surname, String username, String password, String type) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCourses(ArrayList<Integer> courses) {
        this.courses = courses;
    }

    @Override
    public String toString() {
        return "User{" +
                "UID='" + UID + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", type='" + type + '\'' +
                ", courses=" + courses +
                '}';
    }
}
