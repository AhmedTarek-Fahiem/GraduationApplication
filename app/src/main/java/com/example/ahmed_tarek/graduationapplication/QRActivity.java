package com.example.ahmed_tarek.graduationapplication;

import android.Manifest;
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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
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
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Ahmed_Tarek on 17/11/22.
 */

public class QRActivity extends AppCompatActivity {

    private static final String EXTRA_QR_TEXT = "qr_text";
    private static final String EXTRA_QR_FLAG = "qr_flag";
    private static final String EXTRA_IS_DOCTOR_PRESCRIPTION = "is_doctor_prescription";
    private String enc;
    private ImageView mQRImageView;

    private static class Content {
        String name;
        String quantity;

        private Content(String name, String quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }

    private static class ContentAdapter extends ArrayAdapter<Content> {
        Context mActivityContext;
        int mResource;
        Content mData[] = null;

        private ContentAdapter(Context context, int resource, Content[] content) {
            super(context, resource, content);
            mActivityContext = context;
            mResource = resource;
            mData = content;
        }

        @Override
        public Content getItem(int position) {
            return super.getItem(position);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            convertView = LayoutInflater.from(mActivityContext).inflate(mResource, parent,false);
            TextView mName = convertView.findViewById(R.id.medicineName);
            TextView mQuantity = convertView.findViewById(R.id.medicineQuantity);
            mName.setText(mData[position].name);
            mQuantity.setText(mData[position].quantity);
            return convertView;
        }
    }

    public static class MyDialogFragment extends DialogFragment {

        private String content;
        private static String allMedicines;
        private Content[] contents;

        static MyDialogFragment newInstance(String content, String extraProcessing) {
            MyDialogFragment fragment = new MyDialogFragment();
            Bundle args = new Bundle();
            args.putString("content", content);
            allMedicines = extraProcessing;
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            content = getArguments().getString("content");
            int size = 0, index = 0;
            for (int i = 0; i < content.length(); i++)
                if (content.charAt(i) == '#')
                    size++;
            contents = new Content[size];
            String name, quantity;
            for (int i = 0; i < size; i++) {
                name = content.substring(index, content.indexOf('#', index));
                index = content.indexOf('#', index) + 1;
                if (index < content.length() - 5) {
                    quantity = content.substring(index, content.indexOf('|', index));
                    index = content.indexOf('|', index) + 1;
                } else
                    quantity = content.substring(index, content.length());
                contents[i] = new Content(name, quantity);
            }
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.qr_dialog, container, false);

            ListView mListView = v.findViewById(R.id.qr_content);
            mListView.setAdapter(new ContentAdapter(v.getContext(), R.layout.qr_content, contents));
            Button button = v.findViewById(R.id.confirm);
            if (allMedicines != null)
                v.findViewById(R.id.warning).setVisibility(View.VISIBLE);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                    if (allMedicines != null)
                        startActivity(newIntent(getContext(), allMedicines, false));
                }
            });
            return v;
        }
    }

    public static Intent newIntent(Context packageContext, boolean isDoctorPrescription) {
        Intent intent = new Intent(packageContext, QRActivity.class);
        intent.putExtra(EXTRA_QR_FLAG, true).putExtra(EXTRA_IS_DOCTOR_PRESCRIPTION, isDoctorPrescription);
        return intent;
    }

    public static Intent newIntent(Context packageContext, String qrText, boolean isDoctorPrescription) {
        Intent intent = new Intent(packageContext, QRActivity.class);
        intent.putExtra(EXTRA_QR_FLAG, false).putExtra(EXTRA_QR_TEXT, qrText).putExtra(EXTRA_IS_DOCTOR_PRESCRIPTION, isDoctorPrescription);
        return intent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            MainActivity.showToast(R.string.permission_fail, getApplicationContext());
        else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            saveExternal();
        else
            MainActivity.showToast(R.string.permission_blocked, getApplicationContext());
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 123);
        else
            saveExternal();
    }

     public static String[] saveQR(Bitmap QR, File path, Context context, boolean isExternal) {
        FileOutputStream stream;
        path.mkdirs();
        File savingDirectory;
        try {
            if (isExternal)
                savingDirectory = new File(path, new SimpleDateFormat("yyyyMMdd_HHmmSS", Locale.US).format(new java.util.Date()) + ".png");
            else
                savingDirectory = new File(path, UserLab.get(context).getUsername() + ".png");
            stream = new FileOutputStream(savingDirectory);
            QR.compress(Bitmap.CompressFormat.PNG, 100, stream);
            savingDirectory.setReadable(true);
            stream.close();
            return new String[] { savingDirectory.getAbsolutePath() };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveExternal() {
        BitmapDrawable drawable = (BitmapDrawable) mQRImageView.getDrawable();
        MediaScannerConnection.scanFile(QRActivity.super.getApplicationContext(), saveQR(drawable.getBitmap(), new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/QR"), this, true), new String[] { "image/jpg" }, null);
        MainActivity.showToast(R.string.save_success, getApplicationContext());
    }

    private void loadQR() {
        try {
            mQRImageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(new ContextWrapper(this.getApplicationContext()).getDir("QR",Context.MODE_PRIVATE).getAbsolutePath(), UserLab.get(this).getUsername() + ".png"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String encrypt(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] key = "ElixirLtd".getBytes("UTF-8");
        md.update(key);
        key = md.digest(key);
        SecretKeySpec newKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

        cipher.init(Cipher.ENCRYPT_MODE, newKey);
        int length = message.length + (128 - message.length % 128);
        byte[] destination = new byte[length];
        System.arraycopy(message, 0, destination, 0, message.length);
        for (int i = message.length; i < length; i++)
            destination[i] = 0;
        /*byte[] res = cipher.doFinal(destination);
        String result = "";
        for (byte re : res)
            result = result.concat(Integer.toString(re));
        Log.e("BYTES", result);*/
        return Base64.encodeToString(cipher.doFinal(destination), Base64.DEFAULT);

    }

    public String decrypt(byte[] bytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] key = "ElixirLtd".getBytes("UTF-8");
        md.update(key);
        key = md.digest(key);

        int length = bytes.length + (128 - bytes.length % 128);
        byte[] codB = new byte[length];
        System.arraycopy(bytes, 0, codB, 0, bytes.length);
        SecretKeySpec newKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, newKey);
        for (int i = bytes.length; i < length; i++)
            codB[i] = 0;
        return new String(cipher.doFinal(codB));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity);
        if (Linker.getInstance(null, null).checkState(getApplicationContext()))
            MainActivity.showToast(R.string.set_complete, getApplicationContext());
        mQRImageView = findViewById(R.id.qrImage);
        Button mSave = findViewById(R.id.save_qr_button);
        FloatingActionButton mInquiry = findViewById(R.id.qr_details);
        if (this.getIntent().getBooleanExtra(EXTRA_IS_DOCTOR_PRESCRIPTION, false))
            findViewById(R.id.disclaimer).setVisibility(View.GONE);
        if (!this.getIntent().getBooleanExtra(EXTRA_QR_FLAG, false)) {
            try {
                enc = encrypt(this.getIntent().getStringExtra(EXTRA_QR_TEXT).getBytes("UTF-8"));
                Log.e("ENCRYPT", this.getIntent().getStringExtra(EXTRA_QR_TEXT) + " _|_ " + enc);
                Bitmap QR = new BarcodeEncoder().createBitmap(new MultiFormatWriter().encode(/*encrypt(this.getIntent().getStringExtra(EXTRA_QR_TEXT).getBytes("UTF-8"))*/ getIntent().getStringExtra(EXTRA_QR_TEXT), BarcodeFormat.QR_CODE,1000,1000));
                mQRImageView.setImageBitmap(QR);
                if (saveQR(QR, new File(new ContextWrapper(this.getApplicationContext()).getDir("QR", Context.MODE_PRIVATE).toString()), this, false) != null)
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(UserLab.get(this).getUsername(), true).apply();
            }
            catch (WriterException | IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        } else
            loadQR();
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        mInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable drawable = (BitmapDrawable) mQRImageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                int[] imageArray = new int[bitmap.getHeight() * bitmap.getWidth()];
                bitmap.getPixels(imageArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                try {
                    Log.e("A7A", decrypt(enc.getBytes()) + " _!_ " /*new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), imageArray)))).getText()*/);
                    Map<DecodeHintType, Object> tmpHintsMap = new EnumMap<>(
                            DecodeHintType.class);
                    tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                    tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS,
                            EnumSet.allOf(BarcodeFormat.class));
                    tmpHintsMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
                    MyDialogFragment.newInstance(/*decrypt(new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), imageArray)))).getText().getBytes())*/new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), imageArray))), tmpHintsMap).getText(), null).show(getSupportFragmentManager().beginTransaction(), "dialog");
                } catch (NotFoundException | ClassNotFoundException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | IOException | IllegalBlockSizeException e) {
                    e.printStackTrace();
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
