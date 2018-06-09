package com.example.ahmed_tarek.graduationapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.PrescriptionDbSchema.PrescriptionTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.CartMedicineDbSchema.CartMedicineTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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

    private PrescriptionCartCursorWrapper queryPrescriptionCart(String tableName, String whereClause, String[] whereArgs, String orderBy) {

        Cursor cursor = mSQLiteDatabase.query(
                tableName,
                null,
                whereClause,
                whereArgs,
                null,
                null
                , orderBy
        );
        return new PrescriptionCartCursorWrapper(cursor);
    }

    public List<Prescription> getSynchronizable(UUID userID, long lastUpdated, long lastPrescription) {
        List<Prescription> prescriptions = new ArrayList<>();

        PrescriptionCartCursorWrapper cursorWrapper = queryPrescriptionCart(PrescriptionTable.NAME, PrescriptionTable.PrescriptionColumns.USER_UUID + " = ? and " + PrescriptionTable.PrescriptionColumns.PRESCRIPTION_DATE + " > ? and " + PrescriptionTable.PrescriptionColumns.PRESCRIPTION_DATE + " <= ?", new String[] { userID.toString(), Long.toString(lastUpdated), Long.toString(lastPrescription) }, null);
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

    public List<Prescription> getPrescriptions(UUID userID) {
        List<Prescription> prescriptions = new ArrayList<>();

        PrescriptionCartCursorWrapper cursorWrapper = queryPrescriptionCart(PrescriptionTable.NAME, PrescriptionTable.PrescriptionColumns.USER_UUID + " = ?", new String[]{ userID.toString() }, PrescriptionTable.PrescriptionColumns.PRESCRIPTION_DATE + " DESC");
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

        PrescriptionCartCursorWrapper cursorWrapper = queryPrescriptionCart(CartMedicineTable.NAME, CartMedicineTable.CartMedicineColumns.PRESCRIPTION_UUID + " = ?", new String[]{prescriptionID.toString()}, null);
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

        cursorWrapper = queryPrescriptionCart(PrescriptionTable.NAME, PrescriptionTable.PrescriptionColumns.PRESCRIPTION_UUID + " = ?", new String[]{ prescriptionID.toString() }, PrescriptionTable.PrescriptionColumns.PRESCRIPTION_DATE + " DESC");
        cursorWrapper.moveToFirst();
        long prescriptionTime = cursorWrapper.getPrescription().getDate().getTime();
        int temp = (int)((currentTime - prescriptionTime) / (1000*60*60*24));
        String repeatDuration = (temp > 7)? ((temp > 30)? "60" : "30"): "7";
//        String repeatDuration = "7";

        cursorWrapper = queryPrescriptionCart(CartMedicineTable.NAME, CartMedicineTable.CartMedicineColumns.PRESCRIPTION_UUID + " = ? and " + CartMedicineTable.CartMedicineColumns.REPEAT_DURATION + " = ?", new String[]{ prescriptionID.toString(), repeatDuration }, null);
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
        contentValues.put(PrescriptionTable.PrescriptionColumns.HISTORY_UUID, prescription.getHistoryID().toString());

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

    boolean sync(Activity activity, JSONArray arr, UUID id) throws JSONException, ParseException, WriterException {
        boolean result = false;

        PreferenceManager.getDefaultSharedPreferences(activity).getLong(UserLab.get(activity).getUsername() + "_recentDate", 0);
        String recentId = null;
        String recentPrescription = null;
        long recentDate = PreferenceManager.getDefaultSharedPreferences(activity).getLong(UserLab.get(activity).getUsername() + "_recentDate", 0);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject prescription = arr.getJSONObject(i);
            JSONObject prescription_details = prescription.getJSONObject("prescription");

            if (prescription_details.getLong(TAG_DATE) > recentDate) {
                recentPrescription = prescription_details.getString(TAG_HISTORY_ID);
                recentId = prescription_details.getString(TAG_ID);
                recentDate = prescription_details.getLong(TAG_DATE);
            }
            mSQLiteDatabase.insert(PrescriptionTable.NAME, null, getContentValues(id.toString(), new Prescription(UUID.fromString(prescription_details.getString(TAG_ID)), new Date(prescription_details.getLong(TAG_DATE)), prescription_details.getDouble(TAG_PRICE), UUID.fromString(prescription_details.getString(TAG_HISTORY_ID)))));
            JSONArray carts = prescription.getJSONArray("carts");
            for (int j = 0; j < carts.length(); j++) {
                JSONObject medicine = carts.getJSONObject(j);
                mSQLiteDatabase.insert(CartMedicineTable.NAME, null, getContentValues(new CartMedicine(UUID.fromString(medicine.getString("prescription_" + TAG_ID)), UUID.fromString(medicine.getString("medicine_" + TAG_ID)), medicine.getInt(TAG_QUANTITY), medicine.getInt(TAG_REPEAT))));
            }
            JSONArray regulars = prescription.getJSONArray("regulars");
            for (int j = 0; j < regulars.length(); j++) {
                JSONObject regular = regulars.getJSONObject(j);
                RegularOrderLab.get(activity).addRegularOrder(UUID.fromString(regular.getString("prescription_" + TAG_ID)), regular.getLong(TAG_STAMP));
                if (regular.getLong(TAG_STAMP) > System.currentTimeMillis())
                    Linker.getInstance(activity, null).initAlarm(activity, regular.getLong(TAG_STAMP));
            }
        }
        if (recentId != null) {
            if (!recentPrescription.equals(MainActivity.SELF_HISTORY_ID))
                result = true;
            if (QRActivity.saveQR(new BarcodeEncoder().createBitmap(new MultiFormatWriter().encode(CartFragment.medicinesToString(getCarts(UUID.fromString(recentId)), activity), BarcodeFormat.QR_CODE, 1000, 1000)), new File(new ContextWrapper(activity).getDir("QR", Context.MODE_PRIVATE).toString()), activity, false) != null) {
                PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(UserLab.get(activity).getUsername(), true).apply();
                PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(UserLab.get(activity).getUsername(), true).apply();
            }
            PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong(UserLab.get(activity).getUsername() + "_recentDate", recentDate).apply();
        }

        return result;
    }
}
