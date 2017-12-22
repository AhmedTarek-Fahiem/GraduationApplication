package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/21.
 */

public class PrescriptionHandler {

    private static PrescriptionHandler sPrescriptionHandler;

    private Prescription mPrescription;
    private List<CartMedicine> mCartMedicines;

    public static PrescriptionHandler get() {
        if (sPrescriptionHandler == null) {
            sPrescriptionHandler = new PrescriptionHandler();
        }
        return sPrescriptionHandler;
    }

    private PrescriptionHandler() {
        mPrescription = new Prescription();
        mCartMedicines = new ArrayList<>();
    }

    public boolean isEmpty() {
        return mCartMedicines.isEmpty();
    }

    public int getMedicinesNumber() {
        return mCartMedicines.size();
    }

    public void addCart(CartMedicine cartMedicine) {
        if (!isExist(cartMedicine.getMedicineID()))
            mCartMedicines.add(cartMedicine);
    }

    public void removeCart(Medicine medicine) {
        for (CartMedicine med : mCartMedicines) {
            if (med.getMedicineID().equals(medicine.getID())) {
                mCartMedicines.remove(med);
                return;
            }
        }
    }

    public boolean isExist(UUID uuid) {
        for (CartMedicine cartMedicine : mCartMedicines) {
            if (cartMedicine.getMedicineID().equals(uuid))
                return true;
        }
        return false;
    }

    public List<CartMedicine> getPrescriptionCartMedicines() {
        return mCartMedicines;
    }

    public void setCartMedicineQuantity(UUID medicineID, int quantity) {
        for (CartMedicine cartMedicine : mCartMedicines) {
            if (cartMedicine.getMedicineID().equals(medicineID))
                cartMedicine.setQuantity(quantity);
        }
    }

    public void setCartMedicineRepeatDuration(UUID medicineID, int duration) {
        for (CartMedicine cartMedicine : mCartMedicines) {
            if (cartMedicine.getMedicineID().equals(medicineID))
                cartMedicine.setRepeatDuration(duration);
        }
    }

    public void setPrescriptionPrice(double prescriptionPrice) {
        mPrescription.setPrice(prescriptionPrice);
    }

    public void prescriptionCommit(Context context) {
        PrescriptionLab.get(context).addPrescription(UserLab.get(context).getUserUUID(), mPrescription, mCartMedicines);
        reset();
    }

    public void reset() {
        sPrescriptionHandler = null;
        mCartMedicines.clear();
    }

}
