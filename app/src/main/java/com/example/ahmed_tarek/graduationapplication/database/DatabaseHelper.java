package com.example.ahmed_tarek.graduationapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.UserDbSchema.UserTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.MedicineDbSchema.MedicineTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.PrescriptionDbSchema.PrescriptionTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.CartMedicineDbSchema.CartMedicineTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.RegularOrderDbSchema.RegularOrderTable;

/**
 * Created by Ahmed_Tarek on 17/12/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DatabaseSchema.DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + UserTable.NAME + "(" +
                UserTable.UserColumns.USER_UUID + " text NOT NULL primary key, " +
                UserTable.UserColumns.USER_NAME + " text NOT NULL UNIQUE, " +
                UserTable.UserColumns.USER_PASSWORD + " text NOT NULL, " +
                UserTable.UserColumns.USER_EMAIL + " text NOT NULL, " +
                UserTable.UserColumns.USER_GENDER + " integer NOT NULL, " +
                UserTable.UserColumns.USER_DATE_OF_BIRTH + " text NOT NULL" +
                ")");

        sqLiteDatabase.execSQL("create table " + MedicineTable.NAME + "(" +
                MedicineTable.MedicineColumns.MEDICINE_UUID + " text NOT NULL primary key, " +
                MedicineTable.MedicineColumns.MEDICINE_NAME + " text NOT NULL, " +
                MedicineTable.MedicineColumns.MEDICINE_CATEGORY + " text NOT NULL, " +
                MedicineTable.MedicineColumns.MEDICINE_FORM + " text NOT NULL, " +
                MedicineTable.MedicineColumns.MEDICINE_ACTIVE_INGREDIENTS + " text NOT NULL, " +
                MedicineTable.MedicineColumns.MEDICINE_PRICE + " real NOT NULL, " +
                MedicineTable.MedicineColumns.MEDICINE_QUANTITY + " integer NOT NULL," +
                MedicineTable.MedicineColumns.MEDICINE_IS_RESTRICTED + " integer NOT NULL" +
                ")" );

        sqLiteDatabase.execSQL("create table " + PrescriptionTable.NAME + "(" +
                PrescriptionTable.PrescriptionColumns.PRESCRIPTION_UUID + " text NOT NULL primary key, " +
                PrescriptionTable.PrescriptionColumns.PRESCRIPTION_DATE + " text NOT NULL, " +
                PrescriptionTable.PrescriptionColumns.PRESCRIPTION_PRICE + " real NOT NULL, " +
                PrescriptionTable.PrescriptionColumns.USER_UUID + " text NOT NULL " +
//                PrescriptionTable.PrescriptionColumns.HISTORY_UUID + "text NOT NULL " +
                ", foreign key (" + PrescriptionTable.PrescriptionColumns.USER_UUID + ") references " +
                UserTable.NAME + "(" + UserTable.UserColumns.USER_UUID + ")" +
//                ", foreign key (" + PrescriptionTable.PrescriptionColumns.HISTORY_UUID + ") references " +
//                HistoryTable.NAME + "(" + HistoryTable.HistoryColumns.HISTORY_UUID + ")" +
                ")");

        sqLiteDatabase.execSQL("create table " + CartMedicineTable.NAME + "(" +
                CartMedicineTable.CartMedicineColumns.PRESCRIPTION_UUID + " text NOT NULL, " +
                CartMedicineTable.CartMedicineColumns.MEDICINE_UUID + " text NOT NULL, " +
                CartMedicineTable.CartMedicineColumns.QUANTITY + " integer NOT NULL, " +
                CartMedicineTable.CartMedicineColumns.REPEAT_DURATION + " integer NOT NULL " +
                ", primary key (" + CartMedicineTable.CartMedicineColumns.PRESCRIPTION_UUID + ", " + CartMedicineTable.CartMedicineColumns.MEDICINE_UUID + ")"+
                ", foreign key (" + CartMedicineTable.CartMedicineColumns.PRESCRIPTION_UUID + ") references " +
                PrescriptionTable.NAME + "(" + PrescriptionTable.PrescriptionColumns.PRESCRIPTION_UUID + ")" +
                ", foreign key (" + CartMedicineTable.CartMedicineColumns.MEDICINE_UUID + ") references " +
                MedicineTable.NAME + "(" + MedicineTable.MedicineColumns.MEDICINE_UUID + ")" +
                ")");

        sqLiteDatabase.execSQL("create table " + RegularOrderTable.NAME + "(" +
                RegularOrderTable.RegularOrderColumns.USER_UUID + " text NOT NULL, " +
                RegularOrderTable.RegularOrderColumns.PRESCRIPTION_UUID + " text NOT NULL, " +
                RegularOrderTable.RegularOrderColumns.FIRE_TIME + " text NOT NULL " +
                ", primary key (" + RegularOrderTable.RegularOrderColumns.PRESCRIPTION_UUID + ", " + RegularOrderTable.RegularOrderColumns.FIRE_TIME + ")" +
                ", foreign key (" + RegularOrderTable.RegularOrderColumns.USER_UUID + ") references " +
                UserTable.NAME + "(" + UserTable.UserColumns.USER_UUID + ")" +
                ", foreign key (" + RegularOrderTable.RegularOrderColumns.PRESCRIPTION_UUID + ") references " +
                PrescriptionTable.NAME + "(" + PrescriptionTable.PrescriptionColumns.PRESCRIPTION_UUID + ")" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}