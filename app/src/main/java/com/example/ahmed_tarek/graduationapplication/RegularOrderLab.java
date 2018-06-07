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

    private Cursor queryRegularOrder(String whereClause, String[] whereArgs, String orderBy){

        Cursor cursor = mSQLiteDatabase.query(
                RegularOrderTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null
                ,orderBy
        );
        return cursor;
    }

    public boolean reminderExists(long timeStamp) {
        Cursor cursor = queryRegularOrder(RegularOrderTable.RegularOrderColumns.USER_UUID + " = ? and " + RegularOrderTable.RegularOrderColumns.FIRE_TIME + " = ?", new String[]{ sharedPreferences.getString("userID", ""), String.valueOf(timeStamp) }, null);
        return cursor.getCount() > 0;
    }

    public String[] getPrescriptionUUIDs(long timeStamp) {
        List<String> prescriptions = new ArrayList<>();

        Cursor cursor = queryRegularOrder(RegularOrderTable.RegularOrderColumns.USER_UUID + " = ? and " + RegularOrderTable.RegularOrderColumns.FIRE_TIME + " = ? ", new String[]{sharedPreferences.getString("userID", ""), String.valueOf(timeStamp)}, null);
        try {
            if (cursor.getCount() == 0)
                return null;

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                prescriptions.add(cursor.getString(1));
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }

        return prescriptions.toArray(new String[prescriptions.size()]);
    }

    public long[] getTimeStamps(String prescriptionId) {
        Cursor cursor;
        if (prescriptionId == null)
            cursor = queryRegularOrder(RegularOrderTable.RegularOrderColumns.USER_UUID + " = ?", new String[]{ sharedPreferences.getString("userID", "") }, null);
        else
            cursor = queryRegularOrder(RegularOrderTable.RegularOrderColumns.USER_UUID + " = ? and " + RegularOrderTable.RegularOrderColumns.PRESCRIPTION_UUID + " = ?", new String[]{ sharedPreferences.getString("userID", ""), prescriptionId }, null);
        long[] timeStamps;
        try {
            if (cursor.getCount() == 0)
                return null;

            timeStamps = new long[cursor.getCount()];
            int i = 0;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                timeStamps[i] = Long.parseLong(cursor.getString(2));
                i++;
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }

        return timeStamps;
    }

    public List<Regular> getRegularOrders(){
        List<Regular> regularOrdersList = new ArrayList<>();

        Cursor cursor = queryRegularOrder(RegularOrderTable.RegularOrderColumns.USER_UUID + " = ?", new String[]{ sharedPreferences.getString("userID", "") }, RegularOrderTable.RegularOrderColumns.FIRE_TIME + " ASC");

        try {
            if (cursor.getCount() == 0)
                return null;

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                regularOrdersList.add(new Regular(UUID.fromString(cursor.getString(1)), Long.parseLong(cursor.getString(2))));
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }

        return regularOrdersList;
    }

    public void addRegularOrder(UUID prescriptionUUID, long timeStamp) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(RegularOrderTable.RegularOrderColumns.USER_UUID, sharedPreferences.getString("userID", ""));
        contentValues.put(RegularOrderTable.RegularOrderColumns.PRESCRIPTION_UUID, prescriptionUUID.toString());
        contentValues.put(RegularOrderTable.RegularOrderColumns.FIRE_TIME, String.valueOf(timeStamp));

        mSQLiteDatabase.insert(RegularOrderTable.NAME, null, contentValues);
    }

    public boolean removeEntry(UUID prescriptionUUID, long timeStamp) {

        mSQLiteDatabase.delete(RegularOrderTable.NAME,
                RegularOrderTable.RegularOrderColumns.USER_UUID + " = ? and " +
                        RegularOrderTable.RegularOrderColumns.PRESCRIPTION_UUID + " = ? and " +
                        RegularOrderTable.RegularOrderColumns.FIRE_TIME + " = ? ",
                new String[]{ sharedPreferences.getString("userID", ""), prescriptionUUID.toString(), String.valueOf(timeStamp) });

        return reminderExists(timeStamp);
    }
}
