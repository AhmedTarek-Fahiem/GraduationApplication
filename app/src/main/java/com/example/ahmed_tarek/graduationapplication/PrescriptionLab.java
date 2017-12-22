package com.example.ahmed_tarek.graduationapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.PrescriptionDbSchema.PrescriptionTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.CartMedicineDbSchema.CartMedicineTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseHelper;

/**
 * Created by Ahmed_Tarek on 17/12/15.
 */

public class PrescriptionLab {

    private static PrescriptionLab sPrescriptionLab;
    private SQLiteDatabase mSQLiteDatabase;

    public static PrescriptionLab get(Context context) {
        if (sPrescriptionLab == null) {
            sPrescriptionLab = new PrescriptionLab(context);
        }
        return sPrescriptionLab;
    }

    private PrescriptionLab(Context context) {
        mSQLiteDatabase = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();
    }


    private class PrescriptionCartCursorWrapper extends android.database.CursorWrapper {

        public PrescriptionCartCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Prescription getPrescription() {
            return new Prescription(UUID.fromString(getString(0)),
                    new Date(getLong(1)),
                    getDouble(2));
        }

        public CartMedicine getCartMedicine() {
            return new CartMedicine(UUID.fromString(getString(0)),
                    UUID.fromString(getString(1)),
                    getInt(2),
                    getInt(3));
        }
    }
    private PrescriptionCartCursorWrapper queryPrescriptionCart(String tableName, String whereClause, String[] whereArgs) {

        Cursor cursor = mSQLiteDatabase.query(
                tableName,
                null,
                whereClause,
                whereArgs,
                null,
                null
                ,null
        );
        return new PrescriptionCartCursorWrapper(cursor);
    }

    public List<Prescription> getPrescriptions(UUID userID) {
        List<Prescription> prescriptions = new ArrayList<>();

        PrescriptionCartCursorWrapper cursorWrapper = queryPrescriptionCart(PrescriptionTable.NAME, PrescriptionTable.PrescriptionColumns.USER_UUID + " = ?", new String[]{ userID.toString() });
        try {
            if (cursorWrapper.getCount() == 0)
                return null;

            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                prescriptions.add(cursorWrapper.getPrescription());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }

        return prescriptions;
    }
    public List<CartMedicine> getCarts(UUID prescriptionID) {
        List<CartMedicine> cartMedicines = new ArrayList<>();

        PrescriptionCartCursorWrapper cursorWrapper = queryPrescriptionCart(CartMedicineTable.NAME, CartMedicineTable.CartMedicineColumns.PRESCRIPTION_UUID + " = ?", new String[]{prescriptionID.toString()});
        try {
            if (cursorWrapper.getCount() == 0)
                return null;

            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                cartMedicines.add(cursorWrapper.getCartMedicine());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }

        return cartMedicines;
    }



    private static ContentValues getContentValues(String userUUID, Prescription prescription) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(PrescriptionTable.PrescriptionColumns.PRESCRIPTION_UUID, prescription.getID().toString());
        contentValues.put(PrescriptionTable.PrescriptionColumns.PRESCRIPTION_DATE, prescription.getDate().getTime());
        contentValues.put(PrescriptionTable.PrescriptionColumns.PRESCRIPTION_PRICE, prescription.getPrice());
        contentValues.put(PrescriptionTable.PrescriptionColumns.USER_UUID, userUUID);

        return contentValues;
    }
    private static ContentValues getContentValues(CartMedicine cartMedicine) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(CartMedicineTable.CartMedicineColumns.PRESCRIPTION_UUID, cartMedicine.getPrescriptionID().toString());
        contentValues.put(CartMedicineTable.CartMedicineColumns.MEDICINE_UUID, cartMedicine.getMedicineID().toString());
        contentValues.put(CartMedicineTable.CartMedicineColumns.QUANTITY, cartMedicine.getQuantity());
        contentValues.put(CartMedicineTable.CartMedicineColumns.REPEAT_DURATION, cartMedicine.getRepeatDuration());

        return contentValues;
    }

    public void addPrescription(UUID userUUID, Prescription prescription, List<CartMedicine> cartMedicines) {

        prescription.setDate();
        ContentValues contentValues = getContentValues(userUUID.toString(), prescription);
        mSQLiteDatabase.insert(PrescriptionTable.NAME, null, contentValues);

        for (CartMedicine cartMedicine : cartMedicines) {
            cartMedicine.setPrescriptionID(prescription.getID());
            ContentValues contentCartValues = getContentValues(cartMedicine);
            mSQLiteDatabase.insert(CartMedicineTable.NAME, null, contentCartValues);
        }
    }

}
