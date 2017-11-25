package com.example.ahmed_tarek.graduationapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/21.
 */

public class CartLab {

    private static CartLab sCartLab;

    private List<Medicine> mCartMedicines;

    public static CartLab get() {
        if (sCartLab == null) {
            sCartLab = new CartLab();
        }
        return sCartLab;
    }

    private CartLab() {
        mCartMedicines = new ArrayList<>();
    }

    public List<Medicine> getCartMedicines() {
        return mCartMedicines;
    }

    public void addMedicine(Medicine medicine) {
        mCartMedicines.add(medicine);
    }

    public void removeMedicine(Medicine medicine) {
        mCartMedicines.remove(medicine);
    }

    public void clearMedicines() {
        mCartMedicines.clear();
    }

    public void setMedicineQuatity(UUID id, int quantity) {
        for (Medicine medicine : mCartMedicines) {
            if (medicine.getID().equals(id)) {
                medicine.setQuantity(quantity);
            }
        }
    }

    public void setMedicineRepeat(UUID id, int repeat) {
        for (Medicine medicine : mCartMedicines) {
            if (medicine.getID().equals(id)) {
                medicine.setRepeat(repeat);
            }
        }
    }

}
