package com.example.ahmed_tarek.graduationapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ahmed_Tarek on 17/11/07.
 */

public class RegistrationFragment extends Fragment {

    private static final String DIALOG_DATE = "dialog_date";

    private static final int REQUEST_CODE = 1;

    Customer customer;

    private EditText mUsername;
    private EditText mPassword;
    private EditText mEMail;
    private Button mDateOfBirth;
    private Spinner mGender;
    private Button mRegistrationButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customer = Customer.getCustomer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.registration_fragment, container, false);

        mUsername = (EditText) view.findViewById(R.id.register_username_label);
        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                customer.setUsername(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mPassword = (EditText) view.findViewById(R.id.register_password_label);
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                customer.setPassword(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mEMail = (EditText) view.findViewById(R.id.register_email_label);
        mEMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                customer.setEMail(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mDateOfBirth = (Button) view.findViewById(R.id.register_date_of_birth_label);
        mDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(customer.getDateOfBirth());
                dialog.setTargetFragment(RegistrationFragment.this, REQUEST_CODE);
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        });

        ///////////////////////////////////////////////////// gender spinner listener

        mRegistrationButton = (Button) view.findViewById(R.id.registration_button);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLogin", true);
                editor.apply();

                Intent i = new Intent(view.getContext(), MainActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);

            customer.setDateOfBirth(date);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd MMM, yyyy", Locale.ENGLISH);

            mDateOfBirth.setText(simpleDateFormat.format(customer.getDateOfBirth()));
        }
    }
}
