package com.example.ahmed_tarek.graduationapplication;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

    private ImageView mQRImageView;
    private Button mShare;
    private Button mSave;

    public static Intent newIntent(Context packageContext, String qrText) {
        Intent intent = new Intent(packageContext, QRActivity.class);
        intent.putExtra(EXTRA_QR_TEXT, qrText);
        return intent;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
    }

     private boolean saveQR(Bitmap QR, File path) {
        FileOutputStream stream;
        path.mkdirs();
        try {
            File savingDirectory = new File(path,"QR.png");
            stream = new FileOutputStream(savingDirectory);
            QR.compress(Bitmap.CompressFormat.PNG, 100, stream);
            savingDirectory.setReadable(true);
            stream.close();
            return true;
        }
        catch (Exception e) {
            Toast.makeText(this, R.string.save_fail, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    private void loadQR() {
        try {
            mQRImageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(new ContextWrapper(getApplicationContext()).getDir("QR",Context.MODE_PRIVATE).getAbsolutePath(), "QR.png"))));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity);
        mQRImageView = (ImageView) findViewById(R.id.qrImage);
        mSave = (Button) findViewById(R.id.save_qr_button);
        mShare = (Button) findViewById(R.id.share_qr_button);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkPermission();
        if (!sharedPreferences.getBoolean("Saved",false)) {
            try {
                Bitmap QR = new BarcodeEncoder().createBitmap(new MultiFormatWriter().encode(getIntent().getStringExtra(EXTRA_QR_TEXT), BarcodeFormat.QR_CODE,1000,1000));
                mQRImageView.setImageBitmap(QR);
                if (saveQR(QR, new File(new ContextWrapper(getApplicationContext()).getDir("QR", Context.MODE_PRIVATE).toString())))
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
                BitmapDrawable drawable = (BitmapDrawable) mQRImageView.getDrawable();
                if(saveQR(drawable.getBitmap(), new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/QR")))
                    Toast.makeText(QRActivity.super.getApplicationContext(), R.string.save_success, Toast.LENGTH_LONG).show();
                MediaScannerConnection.scanFile(QRActivity.super.getApplicationContext(), new String[] { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/QR/QR.png"}, new String[] { "image/png" }, null);
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
