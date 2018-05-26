package com.example.ahmed_tarek.graduationapplication;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/12/14.
 */

public class Prescription {

    private UUID mID;
    private Date mDate;
    private double mPrice;
    private UUID mHistoryID;

    public Prescription() {
        mID = UUID.randomUUID();
        mDate = new Date();
    }

    public Prescription(UUID ID, Date date, double price, UUID historyID) {
        mID = ID;
        mDate = date;
        mPrice = price;
        mHistoryID = historyID;
    }

    public UUID getID() {
        return mID;
    }

    public Date getDate() {
        return mDate;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }

    public UUID getHistoryID() {
        return mHistoryID;
    }

    public void setHistoryID(UUID historyID) {
        mHistoryID = historyID;
    }
}
