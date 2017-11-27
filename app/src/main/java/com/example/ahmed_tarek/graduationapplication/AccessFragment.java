package com.example.ahmed_tarek.graduationapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Ahmed_Tarek on 17/11/23.
 */

public class AccessFragment extends Fragment {

    private Button mLoginButton;
    private Button mRegistrationButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.access_fragment, container, false);

        mLoginButton = (Button) view.findViewById(R.id.start_login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.access_fragment_container, new LoginFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        mRegistrationButton = (Button) view.findViewById(R.id.start_registration_button);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.access_fragment_container, new RegistrationFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });


        return view;
    }
}