package com.example.ahmed_tarek.graduationapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.PrescriptionDbSchema.PrescriptionTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.CartMedicineDbSchema.CartMedicineTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ahmed_Tarek on 17/12/15.
 */

public class PrescriptionLab {

    private static PrescriptionLab sPrescriptionLab;
    private SQLiteDatabase mSQLiteDatabase;

    private static final String TAG_ID = "id";
    private static final String TAG_DATE = "prescription_date";
    private static final String TAG_PRICE = "price";
    private static final String TAG_HISTORY_ID = "history_id";
    private static final String TAG_QUANTITY = "quantity";
    private static final String TAG_REPEAT = "repeat_duration";
    private static final String TAG_STAMP = "fire_time";

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
                    getDouble(2),
                    UUID.fromString(getString(3)));
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
    public List<Prescription> getPrescriptions(UUID userID, long lastUpdate, long lastPrescription) {
        List<Prescription> prescriptions = new ArrayList<>();

        PrescriptionCartCursorWrapper cursorWrapper = queryPrescriptionCart(PrescriptionTable.NAME, PrescriptionTable.PrescriptionColumns.USER_UUID + " = ? and " + PrescriptionTable.PrescriptionColumns.PRESCRIPTION_DATE + " > ? and " + PrescriptionTable.PrescriptionColumns.PRESCRIPTION_DATE + " < ?", new String[]{ userID.toString(), String.valueOf(lastUpdate) , String.valueOf(lastPrescription) });
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

    public List<CartMedicine> getCarts(UUID prescriptionID, long currentTime) {
        List<CartMedicine> cartMedicines = new ArrayList<>();
        PrescriptionCartCursorWrapper cursorWrapper;

        cursorWrapper = queryPrescriptionCart(PrescriptionTable.NAME, PrescriptionTable.PrescriptionColumns.PRESCRIPTION_UUID + " = ?", new String[]{ prescriptionID.toString() });
        cursorWrapper.moveToFirst();
        long prescriptionTime = cursorWrapper.getPrescription().getDate().getTime();
        int temp = (int)((currentTime - prescriptionTime) / (1000*60*60*24));
        String repeatDuration = (temp > 7)? ((temp > 30)? "60" : "30"): "7";
//        String repeatDuration = "7";

        cursorWrapper = queryPrescriptionCart(CartMedicineTable.NAME, CartMedicineTable.CartMedicineColumns.PRESCRIPTION_UUID + " = ? and " + CartMedicineTable.CartMedicineColumns.REPEAT_DURATION + " = ?", new String[]{ prescriptionID.toString(), repeatDuration });
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

        ContentValues contentValues = getContentValues(userUUID.toString(), prescription);
        mSQLiteDatabase.insert(PrescriptionTable.NAME, null, contentValues);

        for (CartMedicine cartMedicine : cartMedicines) {
            cartMedicine.setPrescriptionID(prescription.getID());
            ContentValues contentCartValues = getContentValues(cartMedicine);
            mSQLiteDatabase.insert(CartMedicineTable.NAME, null, contentCartValues);
        }
    }

    void sync(Context context, JSONArray arr, UUID id) throws JSONException, ParseException {
        JSONArray prescriptions = arr.getJSONObject(0).getJSONArray("prescriptions");
        JSONArray carts = arr.getJSONObject(0).getJSONArray("carts");
        for (int i = 0; i < carts.length(); i++) {
            JSONObject o = carts.getJSONObject(i);
            mSQLiteDatabase.insert(CartMedicineTable.NAME, null, getContentValues(new CartMedicine(UUID.fromString(o.getString("prescription_" + TAG_ID)), UUID.fromString(o.getString("medicine_" + TAG_ID)), o.getInt(TAG_QUANTITY), o.getInt(TAG_REPEAT))));
        }

        for (int i = 0; i < prescriptions.length(); i++) {
            JSONObject o = prescriptions.getJSONObject(i);
            mSQLiteDatabase.insert(PrescriptionTable.NAME, null, getContentValues(id.toString(), new Prescription(UUID.fromString(o.getString(TAG_ID)), new Date(o.getLong(TAG_DATE)), o.getDouble(TAG_PRICE), UUID.fromString(o.getString(TAG_HISTORY_ID)))));
        }
        if (arr.getJSONObject(0).getInt("success_regular") == 1) {
            JSONArray regulars = arr.getJSONObject(0).getJSONArray("regulars");
            for (int i = 0; i < regulars.length(); i++) {
                JSONObject o = regulars.getJSONObject(i);
                RegularOrderLab.get(context).addRegularOrder(UUID.fromString(o.getString("prescription_" + TAG_ID)), o.getLong(TAG_STAMP));
            }
        }
    }
}
