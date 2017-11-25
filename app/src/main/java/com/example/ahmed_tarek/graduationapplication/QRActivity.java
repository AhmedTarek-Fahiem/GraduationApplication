package com.example.ahmed_tarek.graduationapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * Created by Ahmed_Tarek on 17/11/22.
 */

public class QRActivity extends AppCompatActivity {

    ImageView mQrImage;
    Button mShare;
    Button mSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity);
        mShare = (Button) findViewById(R.id.share_qr_button);
        mSave = (Button) findViewById(R.id.save_qr_button);
        Log.d("STRING", getIntent().getStringExtra("1"));
        mQrImage = (ImageView) findViewById(R.id.qrImage);
        try {
            mQrImage.setImageBitmap(new BarcodeEncoder().createBitmap(new MultiFormatWriter().encode(getIntent().getStringExtra("1"), BarcodeFormat.QR_CODE,1000,1000)));
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
    }

}
