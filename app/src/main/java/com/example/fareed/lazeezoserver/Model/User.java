package com.example.fareed.lazeezoserver.Model;

/**
 * Created by fareed on 1/19/2018.
 */

public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String IsStaff;
   public User(){

   }

    public User(String name, String password, String phone, String isStaff) {
        Name = name;
        Password = password;
        Phone = phone;
        IsStaff = isStaff;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
