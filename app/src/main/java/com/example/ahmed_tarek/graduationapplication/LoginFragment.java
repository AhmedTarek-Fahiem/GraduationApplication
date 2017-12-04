package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Date;

/**
 * Created by Ahmed_Tarek on 17/11/07.
 */

public class LoginFragment extends Fragment {

    Customer customer;

    private EditText mUsername;
    private EditText mPassword;
    private Button mLoginButton;
    private TextView mErrorMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customer = Customer.getCustomer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.login_fragment, container, false);

        mErrorMessage = (TextView) view.findViewById(R.id.login_error);

        mUsername = (EditText) view.findViewById(R.id.login_username_label);
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

        mPassword = (EditText) view.findViewById(R.id.login_password_label);
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

        mLoginButton = (Button) view.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUsername.getText().length() == 0) {
                    mErrorMessage.setText("Enter your username..");
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if(mPassword.getText().length() == 0) {
                    mErrorMessage.setText("Enter your password..");
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else {
                    if (check(mUsername.getText().toString(), mPassword.getText().toString())) {

                        Intent i = new Intent(view.getContext(), MainActivity.class);
                        startActivity(i);
                        getActivity().finish();
                    } else {
                        mErrorMessage.setText("Wrong username or password...");
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

    private boolean check(String username, String password){

        //check with the database...

        customer.setUsername(mUsername.getText().toString());
        customer.setPassword(mPassword.getText().toString());
        customer.setEMail("my_email@domain.com");        //////////////////////get from database
        customer.setGender("Male");                      //////////////////////get from database
        customer.setDateOfBirth(new Date());             //////////////////////get from database

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
