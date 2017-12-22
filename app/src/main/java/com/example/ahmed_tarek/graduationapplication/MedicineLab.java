package com.example.ahmed_tarek.graduationapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.MedicineDbSchema.MedicineTable;

/**
 * Created by Ahmed_Tarek on 17/11/21.
 */

public class MedicineLab {

    private char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};    ///delete_T

    private static MedicineLab sMedicineLab;

    private SQLiteDatabase mSQLiteDatabase;
    private List<Medicine> mMedicineList;

    /////////////delete_T when connecting to the server
    private static ContentValues getContentValues(Medicine medicine) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_UUID, medicine.getID().toString());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_NAME, medicine.getName());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_CONCENTRATION, medicine.getConcentration());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_CATEGORY, medicine.getCategory());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_FORM, medicine.getForm());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_ACTIVE_INGREDIENTS, medicine.getActiveIngredients());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_PRICE, medicine.getPrice());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_QUANTITY, medicine.getQuantity());

        return contentValues;
    }
    private void addXXX(Medicine medicine) {
        ContentValues contentValues = getContentValues(medicine);
        mSQLiteDatabase.insert(MedicineTable.NAME, null, contentValues);
    }

    public static MedicineLab get(Context context) {
        if (sMedicineLab == null) {
            sMedicineLab = new MedicineLab(context);
        }
        return sMedicineLab;
    }

    private MedicineLab(Context context) {
        mSQLiteDatabase = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();

        /////////////delete_T when connecting to the server
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPreferences.getBoolean("database", false)) {
            for (int i = 0; i < 500; i++) {
                Medicine medicine = new Medicine(letters[i % 26] + "MedicineDbSchema #" + i, 500,
                        "anti-pyretic", "tab", "paracetamol", 3.75 + (i * 2), i * 30);
                addXXX(medicine);
                Log.e("DATA", "add item " + i);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("database", true);
                editor.apply();
            }
        }

        getMedicines();
    }


    private class MedicineCursorWrapper extends CursorWrapper {

        public MedicineCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Medicine getMedicine() {
            return new Medicine(UUID.fromString(getString(0)),
                    getString(1),
                    getInt(2),
                    getString(3),
                    getString(4),
                    getString(5),
                    getDouble(6),
                    getInt(7));
        }
    }
    private MedicineCursorWrapper queryMedicines(String whereClause, String[] whereArgs) {

        Cursor cursor = mSQLiteDatabase.query(
                MedicineTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null
                ,null
        );
        return new MedicineCursorWrapper(cursor);
    }
    private void getMedicines() {
        mMedicineList = new ArrayList<>();

        MedicineCursorWrapper cursorWrapper = queryMedicines(null, null);
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                mMedicineList.add(cursorWrapper.getMedicine());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }
    }

    public List<Medicine> getMedicines(String charSequence) {

        List<Medicine> medicineList = new ArrayList<>();
        for (Medicine medicine : mMedicineList) {
            if (medicine.getName().startsWith(charSequence)) {    // || medicine.getName().contains(charSequence)
                medicineList.add(medicine);
            }
        }
        return medicineList;
    }
    public Medicine getMedicine(UUID id) {

        MedicineCursorWrapper cursorWrapper = queryMedicines(MedicineTable.MedicineColumns.MEDICINE_UUID + " = ?", new String[]{ id.toString()});
        try {
            if (cursorWrapper.getCount() == 0) {
                return null;
            }
            cursorWrapper.moveToFirst();
            return cursorWrapper.getMedicine();
        } finally {
            cursorWrapper.close();
        }
    }

}
