package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

/**
 * Created by Ahmed_Tarek on 17/11/07.
 */

public class LoginFragment extends Fragment {

    private EditText mUsername;
    private EditText mPassword;
    private Button mLoginButton;
    private TextView mErrorMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    mErrorMessage.setText(R.string.empty_username);
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if(mPassword.getText().length() == 0) {
                    mErrorMessage.setText(R.string.empty_password);
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else {
                    if (check(mUsername.getText().toString(), mPassword.getText().toString())) {
                        Intent i = new Intent(view.getContext(), MainActivity.class);
                        startActivity(i);
                        getActivity().finish();
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

        String message = UserLab.get(getActivity()).login(username, password);

        if (message.equals("success")) {
            return true;
        } else {
            mErrorMessage.setText(R.string.invalid_login);
            mErrorMessage.setVisibility(View.VISIBLE);
            return false;
        }
    }
}
