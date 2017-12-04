package com.example.ahmed_tarek.graduationapplication;

import android.app.Activity;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
    private EditText mConfirmPassword;
    private EditText mEMail;
    private Button mDateOfBirth;
    private Spinner mGender;
    private Button mRegistrationButton;
    private TextView mErrorMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customer = Customer.getCustomer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.registration_fragment, container, false);

        mErrorMessage = (TextView) view.findViewById(R.id.registration_error);

        mUsername = (EditText) view.findViewById(R.id.register_username_label);
        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mErrorMessage.setVisibility(View.GONE);
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
                mErrorMessage.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mConfirmPassword = (EditText) view.findViewById(R.id.register_confirm_password_label);
        mConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mErrorMessage.setVisibility(View.GONE);
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
                mErrorMessage.setVisibility(View.GONE);
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

        mGender = (Spinner) view.findViewById(R.id.register_gender_spinner_label);

        mRegistrationButton = (Button) view.findViewById(R.id.registration_button);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUsername.getText().length() == 0) {
                    mErrorMessage.setText("Enter your username..");
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if(mPassword.getText().length() == 0) {
                    mErrorMessage.setText("Enter your password..");
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if(mConfirmPassword.getText().length() == 0) {
                    mErrorMessage.setText("Enter confirm password..");
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if(!(mPassword.getText().toString()).equals(mConfirmPassword.getText().toString())) {
                    mErrorMessage.setText("password not match..");
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if(mEMail.getText().length() == 0) {
                    mErrorMessage.setText("Enter your e-mail..");
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else {
                    if (check(mUsername.getText().toString(), mPassword.getText().toString(), mConfirmPassword.getText().toString(), mEMail.getText().toString())) {

                        Intent i = new Intent(view.getContext(), MainActivity.class);
                        startActivity(i);
                        getActivity().finish();
                    } else {
                        mErrorMessage.setText("Wrong data entered...");
                        mErrorMessage.setVisibility(View.VISIBLE);
                    }
                }
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

            customer.setDateOfBirth(date);//////////////////////////////////////////////// delete when connecting to database

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd MMM, yyyy", Locale.ENGLISH);

            mDateOfBirth.setText(simpleDateFormat.format(customer.getDateOfBirth()));
        }
    }

    private boolean check(String username, String firstPassword, String secondPassword, String email) {

        //check with the database...

        customer.setUsername(mUsername.getText().toString());
        customer.setPassword(mPassword.getText().toString());
        customer.setEMail(mEMail.getText().toString());
        customer.setGender(mGender.getSelectedItemPosition() == 0? "Male" : "Female");

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putBoolean("isLogin", true);

        editor.putString("username", customer.getUsername());
        editor.putString("password", customer.getPassword());
        editor.putString("email", customer.getEMail());
        editor.putString("gender", customer.getGender());

        editor.apply();

        return true;
    }
}
