package com.example.ahmed_tarek.graduationapplication;

import java.util.Date;

/**
 * Created by Ahmed_Tarek on 17/11/21.
 */

public class Customer {

    private static Customer sCustomer;

    private String mUsername;
    private String mPassword;
    private String mEMail;
    private Date mDateOfBirth;
    private char mGender;

    public static Customer getCustomer() {
        if (sCustomer == null) {
            return new Customer();
        }
        return sCustomer;
    }

    public Customer() {
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getEMail() {
        return mEMail;
    }

    public void setEMail(String EMail) {
        mEMail = EMail;
    }

    public Date getDateOfBirth() {
        return mDateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        mDateOfBirth = dateOfBirth;
    }

    public char getGender() {
        return mGender;
    }

    public void setGender(char gender) {
        mGender = gender;
    }
}
