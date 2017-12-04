package com.example.ahmed_tarek.graduationapplication;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

public class MainActivity extends SingleMedicineFragmentActivity {

    @Override
    protected Fragment createFragment() {

        Customer customer = Customer.getCustomer();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        customer.setUsername(sharedPreferences.getString("username", null));
        customer.setPassword(sharedPreferences.getString("password", null));
        customer.setEMail(sharedPreferences.getString("email", null));
        customer.setGender(sharedPreferences.getString("gender", null));

        return new MedicineListFragment();
    }
}