package com.example.ahmed_tarek.graduationapplication;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Ahmed_Tarek on 17/11/05.
 */

public class Cart extends AppCompatActivity {

    Button mCartNextButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity);

        mCartNextButton = (Button) findViewById(R.id.cart_next);

        mCartNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.qr_activity);
            }
        });
    }


}
