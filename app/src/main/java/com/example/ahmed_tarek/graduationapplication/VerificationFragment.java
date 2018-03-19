package com.example.ahmed_tarek.graduationapplication;


import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Rebel Ekko on 18/03/04.
 */

public class VerificationFragment extends Fragment implements AsyncResponse{

    private Button mSend;
    static final String TAG_STALL = "stall";

    void authentication() {
        mSend.setEnabled(false);
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mSend.setEnabled(true);
                        if (task.isSuccessful())
                            MainActivity.showToast("Verification e-mail sent to " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), getContext());
                        else
                            MainActivity.showToast(R.string.verification_failure, getContext());
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.verification_fragment, container, false);
        Button mRefresh = view.findViewById(R.id.refresh);

        mSend = view.findViewById(R.id.resend_verification_email);
        authentication();
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSend.setEnabled(false);
                authentication();
            }
        });
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().getCurrentUser().reload()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn", true).apply();
                                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(UserLab.get(getContext()).getUsername() + "_securityPin", UserLab.get(getContext()).getSecurity_PIN()).apply();
                                        new MainActivity.DatabaseComm(VerificationFragment.this, getActivity(), TAG_STALL).execute();
                                    } else
                                        MainActivity.showToast("Email is still unverified\nCheck your email's inbox\n(" + FirebaseAuth.getInstance().getCurrentUser().getEmail() + ")", getContext());
                                }
                            }
                        });
            }
        });

        return view;
    }

    @Override
    public void processFinish(JSONObject output, String type) {
        startActivity(new Intent(getContext(), MainActivity.class));
        getActivity().finish();
    }
}
