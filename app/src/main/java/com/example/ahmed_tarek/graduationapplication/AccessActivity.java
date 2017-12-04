package com.example.ahmed_tarek.graduationapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by Ahmed_Tarek on 17/11/07.
 */

public class AccessActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if ( !sharedPreferences.getBoolean("isLogin", false) ) {
            setContentView(R.layout.access_activity);

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.access_fragment_container);

            if (fragment == null) {
                fragment = new AccessFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.access_fragment_container, fragment)
                        .commit();
            }
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }


    }
}