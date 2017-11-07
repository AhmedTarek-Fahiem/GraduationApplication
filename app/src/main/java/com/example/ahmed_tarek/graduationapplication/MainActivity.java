package com.example.ahmed_tarek.graduationapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button mSearchSubmitButton;
    Button mDetailsButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSearchSubmitButton = (Button) findViewById(R.id.search_submit);
        mDetailsButton = (Button) findViewById(R.id.medicine_details);


        mSearchSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), Cart.class);
                startActivity(i);
            }
        });


        mDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), MedicineDetails.class);
                startActivity(i);
            }
        });





    }
}
