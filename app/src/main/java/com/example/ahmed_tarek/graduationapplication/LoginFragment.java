package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/07.
 */

public class LoginFragment extends Fragment implements AsyncResponse {

    private EditText mUsername;
    private EditText mPassword;
    private TextView mErrorMessage;
    static final String TAG_LOGIN = "login";
    static final String TAG_PATIENT = "patient";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_DoB = "dob";
    private static final String TAG_GENDER = "gender";

    private FirebaseUser user;

    private boolean checkState() {
        return ((ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || ((ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    public void processFinish(JSONObject output, String type) {
        if (type.equals(TAG_LOGIN))
            if (output != null)
                try {
                    int key = output.getJSONArray(MainActivity.TAG_SUCCESS).getJSONObject(0).getInt(MainActivity.TAG_SUCCESS);
                    if (key == 0)
                        MainActivity.showToast(R.string.wrong_credentials, getContext());
                    else if (key == 1) {
                        JSONObject o = output.getJSONArray(TAG_PATIENT).getJSONObject(0);
                        UserLab.get(getContext()).saveUserData(UUID.fromString(o.getString(RegistrationFragment.TAG_ID)), mUsername.getText().toString(), o.getString(TAG_EMAIL), Date.valueOf(o.getString(TAG_DoB)), o.getString(TAG_GENDER).equals("m"), Integer.valueOf(o.getString(MainActivity.TAG_PIN)));
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(UserLab.get(getContext()).getEMail(), mPassword.getText().toString())
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            user = FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null) {
                                                if (user.isEmailVerified()) {
                                                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn", true).apply();
                                                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(UserLab.get(getContext()).getUsername() + "_securityPin", UserLab.get(getContext()).getSecurity_PIN()).apply();
                                                    startActivity(new Intent(getContext(), MainActivity.class));
                                                    getActivity().finish();
                                                } else
                                                    getFragmentManager().beginTransaction()
                                                            .replace(R.id.access_fragment_container, new VerificationFragment())
                                                            .addToBackStack(null)
                                                            .commit();
                                            }
                                        } else
                                            MainActivity.showToast("Authentication failed", getContext());
                                    }
                                });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            else
                MainActivity.showToast(R.string.connection_failure,getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.login_fragment, container, false);

        mErrorMessage = view.findViewById(R.id.login_error);

        mUsername = view.findViewById(R.id.login_username_label);
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

        mPassword = view.findViewById(R.id.login_password_label);
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

        Button mLoginButton = view.findViewById(R.id.login_button);
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
                    if (checkState())
                        //new MainActivity.DatabaseComm(LoginFragment.this, getActivity(), TAG_LOGIN).execute(new String[] { "http://ahmedgesraha.ddns.net/login.php", mUsername.getText().toString(), mPassword.getText().toString() });
                        new MainActivity.DatabaseComm(LoginFragment.this, getActivity(), TAG_LOGIN).execute(new String[] { MainActivity.LINK + "login", mUsername.getText().toString(), mPassword.getText().toString() });
                    else
                        MainActivity.showToast(R.string.update_required, getContext());
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
}
