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
    private int mQuantity;
    private int repeat;

    public Medicine(String name, int concentration, String category, String form, String activeIngredients, double price, int quantity) {   ///delete_T  generate medicine id only when we want to generate an imagine data
        this(UUID.randomUUID(), name, concentration, category, form, activeIngredients, price, quantity);
    }

    public Medicine(UUID id, String name, int concentration, String category, String form, String activeIngredients, double price, int quantity) {
        mID = id;
        mName = name;
        mConcentration = concentration;
        mCategory = category;
        mForm = form;
        mActiveIngredients = activeIngredients;
        mPrice = price;
        mQuantity = quantity;
    }

    public UUID getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getForm() {
        return mForm;
    }

    public int getConcentration() {
        return mConcentration;
    }

    public String getActiveIngredients() {
        return mActiveIngredients;
    }

    public double getPrice() {
        return mPrice;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }
}
