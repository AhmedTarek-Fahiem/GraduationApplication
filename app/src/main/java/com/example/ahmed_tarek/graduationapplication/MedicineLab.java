package com.example.ahmed_tarek.graduationapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.ahmed_tarek.graduationapplication.database.DatabaseHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.example.ahmed_tarek.graduationapplication.database.DatabaseSchema.MedicineDbSchema.MedicineTable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ahmed_Tarek on 17/11/21.
 */

public class MedicineLab {

    private static MedicineLab sMedicineLab;
    private SQLiteDatabase mSQLiteDatabase;
    private List<Medicine> mMedicineList;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VERSION = "version";
    private static final String TAG_TIMESTAMP = "ver";
    private static final String TAG_MEDICINES = "medicines";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_FORM = "form";
    private static final String TAG_INGREDIENT = "active_ingredient";
    private static final String TAG_CONCENTRATION = "concentration";
    private static final String TAG_PRICE = "price";
    private static final String TAG_QUANTITY = "quantity";

    class LoadAll extends AsyncTask<String, String, JSONArray> {

        private ProgressDialog pDialog;
        private Context context;

        private LoadAll(Context context) {
            this.context = context;
        }

        private JSONObject makeHttpRequest(String url, String method, List<NameValuePair> params) {

            InputStream is = null;
            JSONObject jObj = null;
            String json = "";
            try {
                if (method.equals("POST")){
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                } else if (method.equals("GET")){
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    String paramString = URLEncodedUtils.format(params, "utf-8");
                    url += "?" + paramString;
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                json = sb.toString();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
            try {
                jObj = new JSONObject(json);
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            return jObj;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONArray doInBackground(String... args) {
            JSONObject json = makeHttpRequest(args[0], "GET", new ArrayList<NameValuePair>());

            try {
                if (args[1].equals(TAG_MEDICINES) && json.getInt(TAG_SUCCESS) == 1)
                    return json.getJSONArray(TAG_MEDICINES);
                else if (args[1].equals(TAG_VERSION) && json.getInt(TAG_SUCCESS) == 1)
                    return json.getJSONArray(TAG_VERSION);
                else
                    Log.e("FAILURE", "Connection to database failed");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONArray arr) {
            pDialog.dismiss();
        }
    }

    private boolean checkState(Context context) {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    private static ContentValues getContentValues(Medicine medicine) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_UUID, medicine.getID().toString());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_NAME, medicine.getName());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_CONCENTRATION, medicine.getConcentration());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_CATEGORY, medicine.getCategory());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_FORM, medicine.getForm());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_ACTIVE_INGREDIENTS, medicine.getActiveIngredients());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_PRICE, medicine.getPrice());
        contentValues.put(MedicineTable.MedicineColumns.MEDICINE_QUANTITY, medicine.getQuantity());
        return contentValues;
    }

    private void update(Context context, String version, boolean isUpdate) throws ExecutionException, InterruptedException, JSONException {
        JSONArray medicinesList = new LoadAll(context).execute("http://ahmedgesraha.ddns.net/get_medicines.php", TAG_MEDICINES).get();
        formatTable();
        for (int i = 0; i < medicinesList.length(); i++) {
            JSONObject o = medicinesList.getJSONObject(i);
            Log.e("DATA", "add item " + o.getString(TAG_NAME));
            Medicine medicine = new Medicine(o.getString(TAG_NAME), Integer.parseInt(o.getString(TAG_CONCENTRATION)), o.getString(TAG_CATEGORY), o.getString(TAG_FORM), o.getString(TAG_INGREDIENT), Double.parseDouble(o.getString(TAG_PRICE)), Integer.parseInt(o.getString(TAG_QUANTITY)));
            addXXX(medicine);
        }
        if (!isUpdate) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("database", true).apply();
            Log.e("NOT UPDATE", "this is not an update (first time)");
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("version", version).apply();
    }

    private void addXXX(Medicine medicine) {
        ContentValues contentValues = getContentValues(medicine);
        mSQLiteDatabase.insert(MedicineTable.NAME, null, contentValues);
    }

    public static MedicineLab get(Context context) {
        if (sMedicineLab == null) {
            sMedicineLab = new MedicineLab(context);
        }
        return sMedicineLab;
    }

    private MedicineLab(Context context) {
        mSQLiteDatabase = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();
        String version = null;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (checkState(context)) {
                try {
                    version = new LoadAll(context).execute("http://ahmedgesraha.ddns.net/get_version.php", TAG_VERSION).get().getJSONObject(0).getString(TAG_TIMESTAMP);
                    Log.e("NICE", "smoooooth! " + version);
                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }
                if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("database", false))
                    try {
                        update(context, version, false);
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                else if (!version.equals(PreferenceManager.getDefaultSharedPreferences(context).getString("version", null))) {
                    try {
                        update(context, version, true);
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else
                Toast.makeText(context, R.string.update_required, Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(context, R.string.no_permission, Toast.LENGTH_LONG).show();
        getMedicines();
    }

    private class MedicineCursorWrapper extends CursorWrapper {

        public MedicineCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Medicine getMedicine() {
            return new Medicine(UUID.fromString(getString(0)),
                    getString(1),
                    getInt(2),
                    getString(3),
                    getString(4),
                    getString(5),
                    getDouble(6),
                    getInt(7));
        }
    }
    private MedicineCursorWrapper queryMedicines(String whereClause, String[] whereArgs) {

        Cursor cursor = mSQLiteDatabase.query(
                MedicineTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null
                ,null
        );
        return new MedicineCursorWrapper(cursor);
    }
    private void getMedicines() {
        mMedicineList = new ArrayList<>();

        MedicineCursorWrapper cursorWrapper = queryMedicines(null, null);
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                mMedicineList.add(cursorWrapper.getMedicine());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }
    }

    public List<Medicine> getMedicines(String charSequence) {

        List<Medicine> medicineList = new ArrayList<>();
        for (Medicine medicine : mMedicineList) {
            if (medicine.getName().startsWith(charSequence)) {    // || medicine.getName().contains(charSequence)
                medicineList.add(medicine);
            }
        }
        return medicineList;
    }
    public Medicine getMedicine(UUID id) {

        MedicineCursorWrapper cursorWrapper = queryMedicines(MedicineTable.MedicineColumns.MEDICINE_UUID + " = ?", new String[]{ id.toString()});
        try {
            if (cursorWrapper.getCount() == 0) {
                return null;
            }
            cursorWrapper.moveToFirst();
            return cursorWrapper.getMedicine();
        } finally {
            cursorWrapper.close();
        }
    }

    public void formatTable() {
        mSQLiteDatabase.delete(MedicineTable.NAME, null, null);
    }
}
