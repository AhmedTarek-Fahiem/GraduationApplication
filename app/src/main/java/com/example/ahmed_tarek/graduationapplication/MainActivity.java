package com.example.ahmed_tarek.graduationapplication;

import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

public class MainActivity extends SingleMedicineFragmentActivity {

    @Override
    protected Fragment createFragment() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("Saved", false)) {
        }
        return new MedicineListFragment();
    }
}