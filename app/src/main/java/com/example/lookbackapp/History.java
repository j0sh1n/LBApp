package com.example.lookbackapp;

public class History {

    public String time;
    public String name;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return "TIME : " + time + "\nNAME : " + name;
    }
}