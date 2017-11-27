package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Ahmed_Tarek on 17/11/22.
 */

public class QRActivity extends AppCompatActivity {

    private static final String EXTRA_QR_TEXT = "qr_text";

    private ImageView mQRImage;
    private Button mShare;
    private Button mSave;
    private Bitmap QR;

    public static Intent newIntent(Context packageContext, String qrText) {
        Intent intent = new Intent(packageContext, QRActivity.class);
        intent.putExtra(EXTRA_QR_TEXT, qrText);
        return intent;
    }

    private boolean saveInternalQR(Bitmap QR, File path) {
        FileOutputStream stream;
        path.mkdirs();
        Log.d("STRING", path.getAbsolutePath());
        try {
            Log.d("STRING", "entered slipstream");
            stream = new FileOutputStream(new File(path,"QR.png"));
            QR.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            return true;
        }
        catch (Exception e) {
            Toast.makeText(this, R.string.fail, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    private void saveExternalQR(Bitmap QR, File path) {

    }

    private void loadQR() {
        try {
            mQRImage.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(new ContextWrapper(getApplicationContext()).getDir("QR",Context.MODE_PRIVATE).getAbsolutePath(), "QR.png"))));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity);
        mQRImage = (ImageView) findViewById(R.id.qrImage);
        mSave = (Button) findViewById(R.id.save_qr_button);
        mShare = (Button) findViewById(R.id.share_qr_button);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean("Saved",false)) {
            try {
                QR = new BarcodeEncoder().createBitmap(new MultiFormatWriter().encode(getIntent().getStringExtra(EXTRA_QR_TEXT), BarcodeFormat.QR_CODE,1000,1000));
                mQRImage.setImageBitmap(QR);
                if (saveInternalQR(QR, new File(new ContextWrapper(getApplicationContext()).getDir("QR", Context.MODE_PRIVATE).toString())))
                    sharedPreferences.edit().putBoolean("Saved", true)
                            .apply();
            }
            catch (WriterException e) {
                e.printStackTrace();
            }
        }
        else
            loadQR();

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(saveInternalQR(QR, new File(Environment.getExternalStorageDirectory().toString() + "/QR")))
                    Toast.makeText(QRActivity.super.getApplicationContext(), R.string.success, Toast.LENGTH_LONG).show();
            }
        });

        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //In progress
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
