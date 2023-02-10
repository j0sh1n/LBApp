package com.example.lookbackapp;

public class User {
    private String email;
    private String pass;
    private String covStat;

    public User(){

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

    public String getCovStat() {
        return covStat;
    }

    public void setCovStat(String covStat) {
        this.covStat = covStat;
    }

    public String toString(){
        return "Email : " + email + "\n Pass : " + pass + "\n Covid Status : " + covStat;
    }
}
