package com.example.ahmed_tarek.graduationapplication;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by Ahmed_Tarek on 17/11/22.
 */

public abstract class SingleMedicineFragmentActivity extends FragmentActivity {

    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentById(R.id.main_fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container, fragment)
                    .commit();
        }

    }
}
