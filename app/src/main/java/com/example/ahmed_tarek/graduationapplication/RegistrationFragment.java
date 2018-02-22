package com.example.ahmed_tarek.graduationapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Ahmed_Tarek on 17/11/07.
 */

public class RegistrationFragment extends Fragment implements AsyncResponse{

    private static final String DIALOG_DATE = "dialog_date";
    private static final int REQUEST_CODE = 1;
    static final String TAG_REGISTRATION = "registration";
    static final String TAG_ERROR = "error";

    Date mUserDateOfBirth;

    private EditText mUsername;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private EditText mEMail;
    private Button mDateOfBirth;
    private Spinner mGender;
    private Button mRegistrationButton;
    private TextView mErrorMessage;

    private boolean checkState() {
        return ((ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || ((ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    public void processFinish(JSONArray output, String type) {
        if (type.equals(TAG_REGISTRATION)) {
            if (output != null) {
                try {
                    int error = output.getJSONObject(0).getInt(TAG_ERROR);
                    if (error == 0) {
                        //TODO: get the user id and save it at UserLab
                        //UserLab.get(getContext()).saveUserData(, mUsername.getText().toString(), mPassword.getText().toString(), mEMail.getText().toString(), mUserDateOfBirth, mGender.getSelectedItemPosition() == 0, 0);
                        startActivity(new Intent(getContext(), MainActivity.class));
                        getActivity().finish();
                    } else if (error == 1)
                        MainActivity.showToast(R.string.username_exists, getContext());
                    else if (error == 2)
                        MainActivity.showToast(R.string.email_linked, getContext());
                    else if (error == 3)
                        MainActivity.showToast(R.string.database_error, getContext());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                DatePickerFragment dialog = DatePickerFragment.newInstance(mUserDateOfBirth);
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
                    mErrorMessage.setText(R.string.empty_username);
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if (mPassword.getText().length() == 0) {
                    mErrorMessage.setText(R.string.empty_password);
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if (mConfirmPassword.getText().length() == 0) {
                    mErrorMessage.setText(R.string.empty_confirmation_password);
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if (!(mPassword.getText().toString()).equals(mConfirmPassword.getText().toString())) {
                    mErrorMessage.setText(R.string.password_match);
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if (mEMail.getText().length() == 0) {
                    mErrorMessage.setText(R.string.empty_email);
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else if (mUserDateOfBirth == null) {
                    mErrorMessage.setText(R.string.empty_date);
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else {
                    if (checkState())
                        new MainActivity.DatabaseComm(RegistrationFragment.this, getActivity()).execute("http://ahmedgesraha.ddns.net/register.php", TAG_REGISTRATION, mUsername.getText().toString(), mPassword.getText().toString(), mEMail.getText().toString(), new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(mUserDateOfBirth), mGender.getSelectedItem().toString());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            mUserDateOfBirth = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd MMM, yyyy", Locale.ENGLISH);
            mDateOfBirth.setText(simpleDateFormat.format(mUserDateOfBirth));
        }
    }

}
