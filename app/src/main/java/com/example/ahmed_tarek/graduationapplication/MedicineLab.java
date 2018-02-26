package com.example.ahmed_tarek.graduationapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.example.ahmed_tarek.graduationapplication.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.MedicineDbSchema.MedicineTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ahmed_Tarek on 17/11/21.
 */

public class MedicineLab {

    private static MedicineLab sMedicineLab;
    private static SQLiteDatabase mSQLiteDatabase;
    private List<Medicine> mMedicineList;
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_FORM = "form";
    private static final String TAG_INGREDIENTS = "active_ingredients";
    private static final String TAG_PRICE = "price";
    private static final String TAG_QUANTITY = "quantity";

    private static ContentValues getContentValues(Medicine medicine) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_UUID, medicine.getID().toString());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_NAME, medicine.getName());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_CATEGORY, medicine.getCategory());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_FORM, medicine.getForm());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_ACTIVE_INGREDIENTS, medicine.getActiveIngredients());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_PRICE, medicine.getPrice());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_QUANTITY, medicine.getQuantity());
        return contentValues;
    }

    void update(Context context, JSONArray medicinesList) throws ExecutionException, InterruptedException, JSONException {
        formatTable();
        for (int i = 0; i < medicinesList.length(); i++) {
            JSONObject o = medicinesList.getJSONObject(i);
            addXXX(new Medicine(UUID.fromString(o.getString(TAG_ID)), o.getString(TAG_NAME), o.getString(TAG_CATEGORY), o.getString(TAG_FORM), o.getString(TAG_INGREDIENTS), Double.parseDouble(o.getString(TAG_PRICE)), Integer.parseInt(o.getString(TAG_QUANTITY))));
        }
        MainActivity.showToast(R.string.update_complete, context);
        get(context).getMedicines();
    }

    private static void addXXX(Medicine medicine) {
        ContentValues contentValues = getContentValues(medicine);
        mSQLiteDatabase.insert(MedicineTable.NAME, null, contentValues);
    }

    public static MedicineLab get(Context context) {
        if (sMedicineLab == null)
            sMedicineLab = new MedicineLab(context);
        return sMedicineLab;
    }

    private MedicineLab(Context context) {
        mSQLiteDatabase = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();
        getMedicines();
    }

    private class MedicineCursorWrapper extends CursorWrapper {

        public MedicineCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Medicine getMedicine() {
            return new Medicine(UUID.fromString(getString(0)),
                    getString(1),
                    getString(2),
                    getString(3),
                    getString(4),
                    getDouble(5),
                    getInt(6));
        }
    }
    private MedicineCursorWrapper queryMedicines(String whereClause, String[] whereArgs) {

        Cursor cursor = mSQLiteDatabase.query(
                MedicineTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
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
            if (medicine.getName().toLowerCase().startsWith(charSequence.toLowerCase())) {    // || medicine.getName().contains(charSequence)
                medicineList.add(medicine);
            }
        }
        return medicineList;
    }

    public Medicine getMedicine(UUID id) {

        MedicineCursorWrapper cursorWrapper = queryMedicines(MedicineTable.MedicineColumns.MEDICINE_UUID + " = ?", new String[]{id.toString()});
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

    private void formatTable() {
        mSQLiteDatabase.delete(MedicineTable.NAME, null, null);
    }
}