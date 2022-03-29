package com.example.lookbackapp;

public class User {
    private String email;
    private String pass;
    private String covStat;
    private String lname;
    private String fname;
    private String gender;
    private String address;
    private String company;
    private String employment;
    public User(){

    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmployment() {
        return employment;
    }

    public void setEmployment(String employment) {
        this.employment = employment;
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
