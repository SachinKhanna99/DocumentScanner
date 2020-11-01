package com.example.myabc.model;

public class Users {
    String name,email,premium;


    Users(){}

    public Users(String name, String email, String premium) {
        this.name = name;
        this.email = email;
        this.premium = premium;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPremium() {
        return premium;
    }

    public void setPremium(String premium) {
        this.premium = premium;
    }
}

