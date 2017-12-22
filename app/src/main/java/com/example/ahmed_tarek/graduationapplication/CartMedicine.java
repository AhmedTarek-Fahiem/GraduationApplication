package com.example.ahmed_tarek.graduationapplication;

import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/12/14.
 */

public class CartMedicine {

    private UUID mPrescriptionID;
    private UUID mMedicineID;
    private int mQuantity;
    private int mRepeatDuration;

    public CartMedicine(UUID medicineID) {
        mMedicineID = medicineID;
        mQuantity = 1 ;
        mRepeatDuration = 0;
    }

    public CartMedicine(UUID prescriptionID, UUID medicineID, int quantity, int repeatDuration) {
        mPrescriptionID = prescriptionID;
        mMedicineID = medicineID;
        mQuantity = quantity ;
        mRepeatDuration = repeatDuration;
    }

    public void setPrescriptionID(UUID prescriptionID) {
        mPrescriptionID = prescriptionID;
    }

    public UUID getPrescriptionID() {
        return mPrescriptionID;
    }

    public UUID getMedicineID() {
        return mMedicineID;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void setQuantity(int quantity) {
        mQuantity = quantity;
    }

    public int getRepeatDuration() {
        return mRepeatDuration;
    }

    public void setRepeatDuration(int repeatDuration) {
        mRepeatDuration = repeatDuration;
    }
}
