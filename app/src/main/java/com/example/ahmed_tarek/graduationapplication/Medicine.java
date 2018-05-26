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
    private String mActiveIngredients;
    private double mPrice;
    private int mQuantity;
    private int isRestricted;

    public Medicine(String name, String category, String form, String activeIngredients, double price, int quantity, int isRestricted) {   ///delete_T  generate medicine id only when we want to generate an imagine data
        this(UUID.randomUUID(), name, category, form, activeIngredients, price, quantity, isRestricted);
    }

    public Medicine(UUID id, String name, String category, String form, String activeIngredients, double price, int quantity, int isRestricted) {
        mID = id;
        mName = name;
        mCategory = category;
        mForm = form;
        mActiveIngredients = activeIngredients;
        mPrice = price;
        mQuantity = quantity;
        this.isRestricted = isRestricted;
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

    public String getActiveIngredients() {
        return mActiveIngredients;
    }

    public double getPrice() {
        return mPrice;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public int getIsRestricted() {
        return isRestricted;
    }
}
