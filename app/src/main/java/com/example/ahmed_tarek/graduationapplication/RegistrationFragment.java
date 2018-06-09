package com.example.ahmed_tarek.graduationapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONException;
import org.json.JSONObject;

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

    static final String TAG_RESULT = "result";
    static final String TAG_ERROR = "error";
    static final String TAG_ID = "id";

    Date mUserDateOfBirth;

    private EditText mUsername;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private EditText mEmail;
    private Button mDateOfBirth;
    private Spinner mGender;
    private TextView mErrorMessage;
    private Button mRegistrationButton;

    @Override
    public void processFinish(JSONObject output, String type) {
        if (output != null) {
            try {
                int error;
                if (type.equals(MainActivity.TAG_VERIFY)) {
                    error = output.getJSONArray(TAG_RESULT).getJSONObject(0).getInt(TAG_ERROR);
                    if (error == 0) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            mRegistrationButton.setEnabled(false);
                                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(mUsername.getText().toString())
                                                    .build())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                                try {
                                                                    new MainActivity.DatabaseComm(RegistrationFragment.this, getActivity(), MainActivity.TAG_REGISTRATION).execute(new JSONObject().put("username", mUsername.getText().toString()).put("password", mPassword.getText().toString()).put("email", mEmail.getText().toString()).put("dob", mUserDateOfBirth.getTime()).put("gender", mGender.getSelectedItem().toString().equals("Male")?"m":"f"));
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                        }
                                                    });
                                        } else
                                            MainActivity.showToast("Creation failed", getContext());
                                    }
                                });
                    } else if (error == 1)
                        MainActivity.showToast(R.string.username_exists, getContext());
                    else if (error == 2)
                        MainActivity.showToast(R.string.email_linked, getContext());
                } else if (type.equals(MainActivity.TAG_REGISTRATION)) {
                    error = output.getInt(TAG_ERROR);
                    String id = output.getJSONArray(LoginFragment.TAG_PATIENT).getJSONObject(0).getString(TAG_ID);
                    if (error == 0) {
                        UserLab.get(getContext()).saveUserData(UUID.fromString(id), mUsername.getText().toString(), mEmail.getText().toString(), mUserDateOfBirth, mGender.getSelectedItemPosition() == 0, 0);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.access_fragment_container, new VerificationFragment())
                                .addToBackStack(null)
                                .commit();
                    } else if (error == 1)
                        MainActivity.showToast(R.string.database_error, getContext());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else
            MainActivity.showToast(R.string.connection_failure, getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.registration_fragment, container, false);

        mErrorMessage = view.findViewById(R.id.registration_error);

        mUsername = view.findViewById(R.id.register_username_label);
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

        mPassword = view.findViewById(R.id.register_password_label);
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

        mConfirmPassword = view.findViewById(R.id.register_confirm_password_label);
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

        mEmail = view.findViewById(R.id.register_email_label);
        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mErrorMessage.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mDateOfBirth = view.findViewById(R.id.register_date_of_birth_label);
        mDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mUserDateOfBirth);
                dialog.setTargetFragment(RegistrationFragment.this, REQUEST_CODE);
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        });

        mGender = view.findViewById(R.id.register_gender_spinner_label);

        mRegistrationButton = view.findViewById(R.id.registration_button);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUsername.getText().length() < 5 || mUsername.getText().length() > 20 || mPassword.getText().length() < 6 || mPassword.getText().length() > 20 || mConfirmPassword.getText().length() == 0 || !(mPassword.getText().toString()).equals(mConfirmPassword.getText().toString()) || mEmail.getText().length() == 0 || !Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches() || mUserDateOfBirth == null || !mUsername.getText().toString().matches("[a-zA-Z0-9_]+")) {
                    if (mUsername.getText().length() < 5 || mUsername.getText().length() > 20)
                        mErrorMessage.setText(R.string.empty_username);
                    else if (mPassword.getText().length() < 6 || mPassword.getText().length() > 20)
                        mErrorMessage.setText(R.string.empty_password);
                    else if (!mUsername.getText().toString().matches("[a-zA-Z0-9_]+"))
                        mErrorMessage.setText(R.string.wrong_regex);
                    else if (mConfirmPassword.getText().length() == 0) {
                        mErrorMessage.setText(R.string.empty_confirmation_password);
                    } else if (!(mPassword.getText().toString()).equals(mConfirmPassword.getText().toString())) {
                        mErrorMessage.setText(R.string.password_match);
                    } else if (mEmail.getText().length() == 0 || !Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches()) {
                        mErrorMessage.setText(R.string.invalid_email);
                    } else if (mUserDateOfBirth == null) {
                        mErrorMessage.setText(R.string.empty_date);
                    }
                    mErrorMessage.setVisibility(View.VISIBLE);
                } else {
                    if (Linker.getInstance(null, null).checkState(getContext()))
                        try {
                            new MainActivity.DatabaseComm(RegistrationFragment.this, getActivity(), MainActivity.TAG_VERIFY).execute(new JSONObject().put("username", mUsername.getText().toString()).put("email", mEmail.getText().toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
