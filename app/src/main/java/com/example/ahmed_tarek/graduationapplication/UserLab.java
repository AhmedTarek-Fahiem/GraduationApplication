package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/12/19.
 */

public class UserLab {

    private static UserLab sUserLab;
    private User mUser;
    private Context mContext;


    public static UserLab get(Context context) {
        if (sUserLab == null) {
            sUserLab = new UserLab(context);
        }
        return sUserLab;
    }

    private UserLab(Context context) {
        mContext = context.getApplicationContext();
        loadUserData();
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
    public int getSecurity_PIN() {
        return mUser.getSecurity_PIN();
    }
    public void setSecurity_PIN(int security_PIN) {
        mUser.setSecurity_PIN(security_PIN);
    }


    public void saveUserData(UUID userID, String username, String password, String email, Date dateOfBirth, boolean gender, int security_PIN) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putBoolean("isLogin", true);
        editor.putString("userID", userID.toString());
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("email", email);
        editor.putLong("date", dateOfBirth.getTime());
        editor.putBoolean("gender", gender);
        editor.putInt("security_pin", security_PIN);
        editor.apply();

        mUser = new User(userID, username, password, email, dateOfBirth, gender, security_PIN);
    }
    public void loadUserData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if ( sharedPreferences.getBoolean("isLogin", true) ) {
            mUser = new User(UUID.fromString(sharedPreferences.getString("userID", "")),
                    sharedPreferences.getString("username", "admin"),
                    sharedPreferences.getString("password", "1234"),
                    sharedPreferences.getString("email", "admin@domain.com"),
                    new Date(sharedPreferences.getLong("date", new Date().getTime())),
                    sharedPreferences.getBoolean("gender", true),
                    sharedPreferences.getInt("security_pin", 0));
        }
    }


    private class User {

        private UUID mUserID;
        private String mUsername;
        private String mPassword;
        private String mEMail;
        private Date mDateOfBirth;
        private boolean mGender;
        private int mSecurity_PIN;


        private User(UUID userID, String username, String password, String email, Date dateOfBirth, boolean gender, int security_PIN) {
            mUserID = userID;
            mUsername = username;
            mPassword = password;
            mEMail = email;
            mDateOfBirth = dateOfBirth;
            mGender = gender;
            mSecurity_PIN = security_PIN;
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

        public int getSecurity_PIN() {
            return mSecurity_PIN;
        }

        public void setSecurity_PIN(int security_PIN) {
            mSecurity_PIN = security_PIN;
        }

    }


}
