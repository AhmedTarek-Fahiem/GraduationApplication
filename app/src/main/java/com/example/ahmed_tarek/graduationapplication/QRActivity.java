package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.Intent;
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

    private static final String EXTRA_QR_TEXT = "qr_text";

    private ImageView mQRImage;
    private Button mShare;
    private Button mSave;

    public static Intent newIntent(Context packageContext, String qrText) {

        Intent intent = new Intent(packageContext, QRActivity.class);
        intent.putExtra(EXTRA_QR_TEXT, qrText);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity);

        mQRImage = (ImageView) findViewById(R.id.qrImage);
        mShare = (Button) findViewById(R.id.share_qr_button);
        mSave = (Button) findViewById(R.id.save_qr_button);

        Log.d("STRING", getIntent().getStringExtra(EXTRA_QR_TEXT));

        try {
            mQRImage.setImageBitmap(new BarcodeEncoder().createBitmap(new MultiFormatWriter().encode(getIntent().getStringExtra(EXTRA_QR_TEXT), BarcodeFormat.QR_CODE,1000,1000)));
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
