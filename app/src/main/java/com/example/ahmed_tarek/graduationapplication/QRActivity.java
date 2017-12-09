package com.example.ahmed_tarek.graduationapplication;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;
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
    private static final String EXTRA_QR_FLAG = "qr_flag";

    private ImageView mQRImageView;
    private Button mSave;
    private FloatingActionButton mInquiry;
    private TextView mInfoContainer;

    public static class Content {
        String name;
        String quantity;

        public Content(String name, String quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }

    public static class ContentAdapter extends ArrayAdapter<Content> {
        Context mActivityContext;
        int mResource;
        Content mData[] = null;

        public ContentAdapter(Context context, int resource, Content[] content) {
            super(context, resource, content);
            mActivityContext = context;
            mResource = resource;
            mData = content;
        }

        @Override
        public Content getItem(int position) {
            return super.getItem(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mActivityContext).inflate(mResource, parent,false);
            TextView mName = (TextView) convertView.findViewById(R.id.medicineName);
            TextView mQuantity = (TextView) convertView.findViewById(R.id.medicineQuantity);
            mName.setText(mData[position].name);
            mQuantity.setText(mData[position].quantity);
            return convertView;
        }
    }

    public static class MyDialogFragment extends DialogFragment {

        private String content;
        private Content[] contents;

        static MyDialogFragment newInstance(String content) {
            MyDialogFragment fragment = new MyDialogFragment();
            Bundle args = new Bundle();
            args.putString("content", content);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            content = getArguments().getString("content");
            int size = 0, index = 0;
            for(int i = 0; i < content.length(); i++)
                if(content.charAt(i) == ',')
                    size++;
            contents = new Content[size];
            String name, quantity;
            for(int i = 0; i < size; i++) {
                name = content.substring(index, content.indexOf(',', index));
                index = content.indexOf(',', index) + 1;
                if(index != content.length() - 1) {
                    quantity = content.substring(index, content.indexOf('&', index));
                    index = content.indexOf('&', index) + 1;
                }
                else
                    quantity = content.substring(index);
                contents[i] = new Content(name, quantity);
                Log.d("NAME", contents[i].name);
                Log.d("QUANTITY", contents[i].quantity);
            }
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.qr_dialog, container, false);

            ListView mListView = (ListView) v.findViewById(R.id.qrContent);
            mListView.setAdapter(new ContentAdapter(v.getContext(), R.layout.qr_content, contents));
            Button button = (Button)v.findViewById(R.id.confirm);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });

            return v;
        }
    }

    public static Intent newIntent(Context packageContext, Boolean flag) {
        Intent intent = new Intent(packageContext, QRActivity.class);
        intent.putExtra(EXTRA_QR_FLAG, flag);
        return intent;
    }

    public static Intent newIntent(Context packageContext, String qrText) {
        Intent intent = new Intent(packageContext, QRActivity.class);
        intent.putExtra(EXTRA_QR_FLAG, false);
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
            e.printStackTrace();
            return false;
        }
    }

    private void loadQR() {
        try {
            mQRImageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(new ContextWrapper(this.getApplicationContext()).getDir("QR",Context.MODE_PRIVATE).getAbsolutePath(), "QR.png"))));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity);
        mQRImageView = (ImageView) findViewById(R.id.qrImage);
        mSave = (Button) findViewById(R.id.save_qr_button);
        mInquiry = (FloatingActionButton) findViewById(R.id.qrInquiry);
        if (!this.getIntent().getBooleanExtra(EXTRA_QR_FLAG, false)) {
            try {
                Bitmap QR = new BarcodeEncoder().createBitmap(new MultiFormatWriter().encode(this.getIntent().getStringExtra(EXTRA_QR_TEXT), BarcodeFormat.QR_CODE,1000,1000));
                mQRImageView.setImageBitmap(QR);
                if (saveQR(QR, new File(new ContextWrapper(this.getApplicationContext()).getDir("QR", Context.MODE_PRIVATE).toString())))
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("Saved", true)
                            .apply();
            }
            catch (WriterException e) {
                e.printStackTrace();
            }
            mInquiry.setVisibility(View.GONE);
        }
        else
            loadQR();
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable drawable = (BitmapDrawable) mQRImageView.getDrawable();
                checkPermission();
                if(saveQR(drawable.getBitmap(), new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/QR"))) {
                    Toast.makeText(QRActivity.super.getApplicationContext(), R.string.save_success, Toast.LENGTH_LONG).show();
                    Snackbar.make(view, "Image Path: Pictures/QR", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                MediaScannerConnection.scanFile(QRActivity.super.getApplicationContext(), new String[] { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/QR/QR.png"}, new String[] { "image/png" }, null);
            }
        });

        mInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getIntent().getBooleanExtra(EXTRA_QR_FLAG,false)) {
                    BitmapDrawable drawable = (BitmapDrawable) mQRImageView.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    int[] imageArray = new int[bitmap.getHeight() * bitmap.getWidth()];
                    bitmap.getPixels(imageArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    String content = null;
                    try {
                        content = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), imageArray)))).getText();
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                    MyDialogFragment.newInstance(content).show(getFragmentManager().beginTransaction(), "dialog");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }
}
