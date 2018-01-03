package com.example.ahmed_tarek.graduationapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.preference.PreferenceManager;

import com.example.ahmed_tarek.graduationapplication.database.DatabaseHelper;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.RegularOrderDbSchema.RegularOrderTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 18/01/04.
 */

public class RegularOrderLab {

    private static RegularOrderLab sRegularOrderLab;
    private SQLiteDatabase mSQLiteDatabase;
    private static SharedPreferences sharedPreferences;

    public static RegularOrderLab get(Context context) {
        if (sRegularOrderLab == null) {
            sRegularOrderLab = new RegularOrderLab(context);
        }
        return sRegularOrderLab;
    }

    private RegularOrderLab(Context context) {
        mSQLiteDatabase = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    private Cursor queryRegularOrder(String whereClause, String[] whereArgs){

        Cursor cursor = mSQLiteDatabase.query(
                RegularOrderTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null
                ,null
        );
        return cursor;
    }

    public boolean isReminderExist(long timeStamp) {
        Cursor cursor = queryRegularOrder(RegularOrderTable.RegularOrderColumns.FIRE_TIME + " = ?", new String[]{ String.valueOf(timeStamp) });
        return cursor.getCount() > 0;
    }

    public List<UUID> getPrescriptionUUIDs(long timeStamp) {
        List<UUID> prescriptions = new ArrayList<>();

        Cursor cursor = queryRegularOrder(RegularOrderTable.RegularOrderColumns.USER_UUID + " = ? and " + RegularOrderTable.RegularOrderColumns.FIRE_TIME + " = ? ", new String[]{ sharedPreferences.getString("userID", ""), String.valueOf(timeStamp) });
        try {
            if (cursor.getCount() == 0)
                return null;

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                prescriptions.add(UUID.fromString(cursor.getString(2)));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return prescriptions;
    }

    public void addRegularOrder(UUID prescriptionUUID, long timeStamp) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(RegularOrderTable.RegularOrderColumns.USER_UUID, sharedPreferences.getString("userID", ""));
        contentValues.put(RegularOrderTable.RegularOrderColumns.PRESCRIPTION_UUID, prescriptionUUID.toString());
        contentValues.put(RegularOrderTable.RegularOrderColumns.FIRE_TIME, String.valueOf(timeStamp));

        mSQLiteDatabase.insert(RegularOrderTable.NAME, null, contentValues);
    }
}
