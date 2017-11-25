package com.example.ahmed_tarek.graduationapplication;

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
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Ahmed_Tarek on 17/11/07.
 */

public class LoginFragment extends Fragment {

    Customer customer;

    private EditText mUsername;
    private EditText mPassword;
    private Button mLoginButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customer = Customer.getCustomer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.login_fragment, container, false);

        mUsername = (EditText) view.findViewById(R.id.login_username_label);
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

        mPassword = (EditText) view.findViewById(R.id.login_password_label);
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

        mLoginButton = (Button) view.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
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
}
