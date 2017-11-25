package com.example.ahmed_tarek.graduationapplication;

import android.support.v4.app.Fragment;

/**
 * Created by Ahmed_Tarek on 17/11/22.
 */

public class QRActivity extends SingleMedicineFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new QRFragment();
    }

}
