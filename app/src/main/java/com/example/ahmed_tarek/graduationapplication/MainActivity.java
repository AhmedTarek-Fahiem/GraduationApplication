package com.example.ahmed_tarek.graduationapplication;

import android.support.v4.app.Fragment;

public class MainActivity extends SingleMedicineFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new MedicineListFragment();
    }
}