package com.example.lookbackapp;

public class Management {
    private String email;
    private String pass;
    private String name;
    private String address;
    private int checkIns;
    private String date;
    private int daysWithoutCovid;

    public int getDaysWithoutCovid() {
        return daysWithoutCovid;
    }

    public void setDaysWithoutCovid(int daysWithoutCovid) {
        this.daysWithoutCovid = daysWithoutCovid;
    }

    public int getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(int checkIns) {
        this.checkIns = checkIns;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Management() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}