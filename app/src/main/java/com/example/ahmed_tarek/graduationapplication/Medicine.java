package com.example.ahmed_tarek.graduationapplication;

import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/21.
 */

public class Medicine {

    private UUID mID;
    private String mName;
    private String mCategory;
    private String mForm;
    private int mConcentration;
    private String mActiveIngredients;
    private double mPrice;
    private int quantity;
    private int repeat;

    public Medicine() {
        mID = UUID.randomUUID();
    }

    public UUID getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getForm() {
        return mForm;
    }

    public void setForm(String form) {
        mForm = form;
    }

    public int getConcentration() {
        return mConcentration;
    }

    public void setConcentration(int concentration) {
        mConcentration = concentration;
    }

    public String getActiveIngredients() {
        return mActiveIngredients;
    }

    public void setActiveIngredients(String activeIngredients) {
        mActiveIngredients = activeIngredients;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }
}
