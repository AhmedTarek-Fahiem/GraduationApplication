package com.example.ahmed_tarek.graduationapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.UserDbSchema.UserTable;
import com.example.ahmed_tarek.graduationapplication.database.DatabaseHelper;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/12/19.
 */

public class UserLab {

    private static UserLab sUserLab;
    private User mUser;
    private SQLiteDatabase mSQLiteDatabase;
    private Context mContext;


    public static UserLab get(Context context) {
        if (sUserLab == null) {
            sUserLab = new UserLab(context);
        }
        return sUserLab;
    }

    private UserLab(Context context) {
        mSQLiteDatabase = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();
        mContext = context.getApplicationContext();
        loadUserData();
    }

    private class UserCursorWrapper extends CursorWrapper {

        public UserCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public User getCustomer() {
            return new User(UUID.fromString(getString(getColumnIndex(UserTable.UserColumns.USER_UUID))),
                    getString(getColumnIndex(UserTable.UserColumns.USER_NAME)),
                    getString(getColumnIndex(UserTable.UserColumns.USER_PASSWORD)),
                    getString(getColumnIndex(UserTable.UserColumns.USER_EMAIL)),
                    new Date(getLong(getColumnIndex(UserTable.UserColumns.USER_DATE_OF_BIRTH))),
                    getInt(getColumnIndex(UserTable.UserColumns.USER_GENDER)) != 0);
        }
    }
    private UserCursorWrapper queryUser(String whereClause, String[] whereArgs) {

        Cursor cursor = mSQLiteDatabase.query(
                UserTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null
                ,null
        );
        return new UserCursorWrapper(cursor);
    }
    public String login(String username, String password) {

        UserCursorWrapper cursorWrapper = queryUser(UserTable.UserColumns.USER_NAME + " = ?", new String[]{username});
        try {
            if (cursorWrapper.getCount() == 1) {
                cursorWrapper.moveToFirst();
                if (cursorWrapper.getCustomer().getPassword().equals(password)) {
                    mUser = cursorWrapper.getCustomer();
                    saveUserData();
                    return "success";
                }
            }
        } finally {
            cursorWrapper.close();
        }
        return "error";
    }

    private static ContentValues getContentValues(User user) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(UserTable.UserColumns.USER_UUID, user.getUserID().toString());
        contentValues.put(UserTable.UserColumns.USER_NAME, user.getUsername());
        contentValues.put(UserTable.UserColumns.USER_PASSWORD, user.getPassword());
        contentValues.put(UserTable.UserColumns.USER_EMAIL, user.getEMail());
        contentValues.put(UserTable.UserColumns.USER_DATE_OF_BIRTH, user.getDateOfBirth().getTime());
        contentValues.put(UserTable.UserColumns.USER_GENDER, user.getGender() ? 1 : 0);

        return contentValues;
    }
    public String register(String username, String password, String email, Date dateOfBirth, boolean gender){

        if (queryUser(UserTable.UserColumns.USER_NAME + " = ?", new String[]{username}).getCount() == 0) {
            if (queryUser(UserTable.UserColumns.USER_EMAIL + " = ?", new String[]{email}).getCount() == 0) {
                mUser = new User(username, password, email, dateOfBirth, gender);

                ContentValues contentValues = getContentValues(mUser);
                mSQLiteDatabase.insert(UserTable.NAME, null, contentValues);

                saveUserData();
                return "success";
            } else {
                return "email";
            }
        } else {
            return "username";
        }
    }

    public UUID getUserUUID() {
        return mUser.getUserID();
    }
    public String getUsername() {
        return mUser.getUsername();
    }
    public String getEMail(){
        return mUser.getEMail();
    }

    private void saveUserData() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        editor.putBoolean("isLogin", true);
        editor.putString("userID", mUser.getUserID().toString());
        editor.putString("username", mUser.getUsername());
        editor.putString("password", mUser.getPassword());
        editor.putString("email", mUser.getEMail());
        editor.putLong("date", mUser.getDateOfBirth().getTime());
        editor.putBoolean("gender", mUser.getGender());

        editor.apply();
    }
    private void loadUserData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if ( sharedPreferences.getBoolean("isLogin", false) ) {
            mUser = new User(UUID.fromString(sharedPreferences.getString("userID", "")),
                    sharedPreferences.getString("username", "admin"),
                    sharedPreferences.getString("password", "1234"),
                    sharedPreferences.getString("email", "admin@domain.com"),
                    new Date(sharedPreferences.getLong("date", new Date().getTime())),
                    sharedPreferences.getBoolean("gender", true));
        }
    }


    private class User {

        private UUID mUserID;
        private String mUsername;
        private String mPassword;
        private String mEMail;
        private Date mDateOfBirth;
        private boolean mGender;


        private User(String username, String password, String email, Date dateOfBirth, boolean gender) {
            this(UUID.randomUUID(), username, password, email, dateOfBirth, gender);
        }
        private User(UUID userID, String username, String password, String email, Date dateOfBirth, boolean gender) {
            mUserID = userID;
            mUsername = username;
            mPassword = password;
            mEMail = email;
            mDateOfBirth = dateOfBirth;
            mGender =gender;
        }


        public UUID getUserID() {
            return mUserID;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getPassword() {
            return mPassword;
        }

        public String getEMail() {
            return mEMail;
        }

        public Date getDateOfBirth() {
            return mDateOfBirth;
        }

        public boolean getGender() {
            return mGender;
        }
    }


}
