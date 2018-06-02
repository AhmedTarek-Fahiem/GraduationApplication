package com.example.ahmed_tarek.graduationapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.WriterException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.commons.net.time.TimeTCPClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

interface TimeResponse {
    void getTime(Long time);
}

public class MainActivity extends SingleMedicineFragmentActivity implements AsyncResponse, TimeResponse, NavigationView.OnNavigationItemSelectedListener, DrawerInterface {


    public static final String LINK = "http://206.189.113.30/api/";
//    public static final String LINK = "http://ahmedgesraha.ddns.net/api/";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_PIN = "setPIN";
    public static final String TAG_VERSION = "getVersion";
    public static final String TAG_TIMESTAMP = "ver";
    public static final String TAG_MEDICINES = "getMedicinesList";
    public static final String TAG_SYNC = "sync";
    public static final String TAG_PRESCRIPTION = "setPrescription";
    public static final String TAG_LOGIN = "login";
    public static final String TAG_REGISTRATION = "register";
    public static final String TAG_VERIFY = "verifyPatient";
    public static final String TAG_STALL = "stall";
    public static final String SELF_HISTORY_ID = "0395e1e0-c60b-4564-8dea-e92fb83bb9ea";



    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView navigationView;
    private static String version = null;
    private static boolean recentQRFlag = true;
    public static boolean notSynced = true;
    private NetworkChangedReceiver receiver;

    public class NetworkChangedReceiver extends BroadcastReceiver {

        private Activity activity;

        NetworkChangedReceiver(Activity activity) {
            this.activity = activity;
        }

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras() != null) {
                    final NetworkInfo ni = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

                    if (ni != null && ni.isConnectedOrConnecting()) {
                        try {
                            new DatabaseComm(MainActivity.this, activity, TAG_VERSION).execute();
                            long lastUpdated = PreferenceManager.getDefaultSharedPreferences(activity).getLong(UserLab.get(getApplicationContext()).getUsername() + "_lastUpdated", 0), lastPrescription = PreferenceManager.getDefaultSharedPreferences(activity).getLong(UserLab.get(getApplicationContext()).getUsername() + "_lastPrescription", 0);
                            if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("database", false)) {
                                if (lastUpdated == lastPrescription)
                                    new DatabaseComm(MainActivity.this, activity, TAG_SYNC).execute(new JSONObject().put("patient", new JSONObject().put("id", UserLab.get(activity).getUserUUID().toString())).put("lastUpdated", lastUpdated));
                                else
                                    new DatabaseComm(MainActivity.this, activity, TAG_SYNC).execute(toJSON(PrescriptionLab.get(activity).getSynchronizable(UserLab.get(activity).getUserUUID(), lastUpdated, lastPrescription), activity, lastUpdated, true));
                                notSynced = false;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    }

    static class OnlineTime extends AsyncTask<String, String, Long> {

        private ProgressDialog pDialog;
        TimeResponse delegate = null;

        OnlineTime(TimeResponse delegate, Activity activity) {
            this.delegate = delegate;
            pDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Fetching time\nPlease wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Long doInBackground(String... strings) {
            Long time = null;
            try {
                TimeTCPClient client = new TimeTCPClient();
                client.setDefaultTimeout(6000);
                client.connect("time-a-wwv.nist.gov");
                time = client.getTime();
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return time;
        }

        @Override
        protected void onPostExecute(Long result) {
            pDialog.dismiss();
            delegate.getTime(result);
        }
    }

    static class DatabaseComm extends AsyncTask <JSONObject, String, JSONObject> {

        private AsyncResponse delegate = null;
        private ProgressDialog pDialog;
        private String type;

        DatabaseComm(AsyncResponse delegate, Activity activity, String type) {
            pDialog = new ProgressDialog(activity);
            this.delegate = delegate;
            this.type = type;
        }

        private JSONObject makeHttpRequest(String url, String method, JSONObject request) {
            InputStream is = null;
            JSONObject jObj = null;
            String json = "";
            Log.e("Request_JSON", "URL: " + url + "  --  Method: " + method + "  --  JSON: " + request.toString());
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                if (method.equals("POST")) {
                    HttpPost httpPost = new HttpPost(url);
                    StringEntity stringEntity = new StringEntity(request.toString());
                    stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    httpPost.setEntity(stringEntity);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                } else if (method.equals("GET")) {
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
                Log.e("JSON Response", json);
                jObj = new JSONObject(json);
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            return jObj;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            switch (type) {
                case TAG_LOGIN:
                    pDialog.setMessage("Logging in\nPlease wait...");
                    break;
                case TAG_VERIFY:
                    pDialog.setMessage("Checking data validity\nPlease wait...");
                    break;
                case TAG_REGISTRATION:
                    pDialog.setMessage("Checking and completing registration\nPlease wait...");
                    break;
                case TAG_PIN:
                    pDialog.setMessage("Updating PIN in database\nPlease wait...");
                    break;
                case TAG_MEDICINES:
                    pDialog.setMessage("Fetching latest medicine updates\nPlease wait...");
                    break;
                case TAG_PRESCRIPTION:
                    pDialog.setMessage("Uploading data\nPlease wait...");
                    break;
                case TAG_STALL:
                    pDialog.setMessage("Verification successful\nRedirecting back to application\nPlease wait...");
                    break;
            }
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            if (!type.equals(TAG_VERSION) && !type.equals(TAG_SYNC))
                pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... args) {
            JSONObject request = new JSONObject(), json = new JSONObject();
            String link = null;
            String method = "POST";
            try {
                switch (type) {
                    case TAG_LOGIN:
                    case TAG_REGISTRATION:
                        link = LINK + TAG_LOGIN;
                        if (type.equals(TAG_REGISTRATION))
                            link = LINK + TAG_REGISTRATION;
                        break;
                    case TAG_VERIFY:
                        link = LINK + TAG_VERIFY;
                        break;
                    case TAG_PIN:
                        link = LINK + TAG_PIN;
                        break;
                    case TAG_SYNC:
                    case TAG_PRESCRIPTION:
                        link = LINK + TAG_SYNC;
                        if (type.equals(TAG_PRESCRIPTION))
                            link = LINK + TAG_PRESCRIPTION;
                        break;
                    case TAG_STALL:
                        try {
                            Thread.sleep(2000);
                            json.put(TAG_SUCCESS, 1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case TAG_MEDICINES:
                        link = LINK + TAG_MEDICINES;
                        method = "GET";
                        break;
                    case TAG_VERSION:
                        link = LINK + TAG_VERSION;
                        method = "GET";
                        break;
                }
                if (!type.equals(TAG_MEDICINES) && !type.equals(TAG_VERSION) && !type.equals(TAG_STALL))
                    request = args[0];
                if (link != null)
                    json = makeHttpRequest(link, method, request);
                if (type.equals(TAG_MEDICINES) && json.getInt(TAG_SUCCESS) == 1)
                    return json;
                else if (type.equals(TAG_VERSION) && json.getInt(TAG_SUCCESS) == 1)
                    return json;
                else if (type.equals(TAG_VERIFY) || type.equals(TAG_REGISTRATION))
                    return json;
                else if (type.equals(TAG_LOGIN))
                    return json;
                else if (type.equals(TAG_PIN) || type.equals(TAG_PRESCRIPTION))
                    return json;
                else if (type.equals(TAG_SYNC) && (json.getInt(TAG_SUCCESS + "_sync_offline") == 1 && json.getInt(TAG_SUCCESS + "_prescription") == 1))
                    return json;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (!type.equals(TAG_VERSION) && !type.equals(TAG_SYNC))
                pDialog.dismiss();
            delegate.processFinish(result, type);
        }
    }

    @Override
    public void getTime(Long time) {
        long flag = PreferenceManager.getDefaultSharedPreferences(this).getLong(UserLab.get(this).getUsername() + "_timeOut", 0) + 21600;
        if (flag > time)
            showToast("To perform this action, thee shall wait " + (flag - time) / 3600 + ":" + (int)((((flag - time) % 3600) / 3600.0) * 60) + " hour(s)", getApplicationContext());
        else {
            try {
                new DatabaseComm(this, MainActivity.this, TAG_PIN).execute(new JSONObject().put("username", UserLab.get(this).getUsername()).put("pin", setMenuPIN()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(UserLab.get(this).getUsername() + "_timeOut", time).apply();
    }

    public static JSONObject toJSON(List<Prescription> prescriptionList, Activity activity, long lastUpdated, boolean isSync) throws JSONException {
        JSONObject request = new JSONObject();
        JSONArray prescriptions = new JSONArray();
        String patient_id = UserLab.get(activity).getUserUUID().toString();
        request.put("patient", new JSONObject().put("id", patient_id));
        if (isSync)
            request.put("lastUpdated", lastUpdated);

        for (Prescription prescription : prescriptionList) {
            JSONObject prescriptionInJson = new JSONObject();

            UUID id = prescription.getID();
            List<CartMedicine> medicines = PrescriptionLab.get(activity).getCarts(id);

            prescriptionInJson.put("prescription", new JSONObject().put("id", id).put("price", prescription.getPrice()).put("prescription_date", prescription.getDate().getTime()).put("history_id", prescription.getHistoryID()).put("patient_id", patient_id));

            prescriptionInJson = cartToJSON(prescriptionInJson, medicines);
            prescriptionInJson = regularToJSON(prescriptionInJson, id, activity);

            if (!isSync)
                return prescriptionInJson;

            prescriptions.put(prescriptionInJson);
        }

        request.put("prescriptions", prescriptions);
        return request;
    }

    private static JSONObject cartToJSON(JSONObject prescription, List<CartMedicine> medicines) throws JSONException{
        int size = medicines.size();
        JSONArray medicinesJson = new JSONArray();

        for (int i = 0; i < size; i++) {
            JSONObject medicine = new JSONObject();
            medicine.put("medicine_id", medicines.get(i).getMedicineID().toString());
            medicine.put("quantity", medicines.get(i).getQuantity());
            medicine.put("repeat_duration", medicines.get(i).getRepeatDuration());
            medicine.put("prescription_id", medicines.get(i).getPrescriptionID());
            medicinesJson.put(medicine);
        }

        prescription.put("cartMedicines", medicinesJson);
        return prescription;
    }

    private static JSONObject regularToJSON(JSONObject prescription, UUID prescription_id, Activity activity) throws JSONException {
        long[] schedules = RegularOrderLab.get(activity).getTimeStamps(prescription_id.toString());
        if (schedules != null) {
            prescription.put("fire_time1", schedules[0]);
            if (schedules.length > 1)
                prescription.put("fire_time2", schedules[1]);
        }
        return prescription;
    }

    @Override
    protected Fragment createFragment() {
        return new SearchFragment();
    }

    private boolean checkState() {
        return ((ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || ((ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    private Snackbar makeSnack(String message) {
        final Snackbar snackbar = Snackbar
                .make(findViewById(R.id.main_drawer_layout), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        return snackbar;
    }

    @Override
    public void processFinish(JSONObject output, String type) {
        if (output != null) {
            switch (type) {
                case TAG_VERSION:
                    try {
                        version = output.getString(TAG_TIMESTAMP);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (checkState()) {
                        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("database", false)) {
                            MedicineLab.get(this);
                            new DatabaseComm(this, MainActivity.this, TAG_MEDICINES).execute();
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("database", true).apply();
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("version", version).apply();
                        } else if (!version.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("version", null))) {
                            new DatabaseComm(this, MainActivity.this, TAG_MEDICINES).execute();
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("version", version).apply();
                        }
                    } else
                        showToast(R.string.update_required, getApplicationContext());
                    break;
                case TAG_MEDICINES:
                    try {
                        MedicineLab.get(this).update(getApplicationContext(), output.getJSONArray("medicines"));
                        long lastUpdated = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong(UserLab.get(getApplicationContext()).getUsername() + "_lastUpdated", 0), lastPrescription = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong(UserLab.get(getApplicationContext()).getUsername() + "_lastPrescription", 0);
                        if (lastUpdated == lastPrescription)
                            new DatabaseComm(MainActivity.this, this, TAG_SYNC).execute(new JSONObject().put("patient", new JSONObject().put("id", UserLab.get(getApplicationContext()).getUserUUID().toString()).put("lastUpdated", lastUpdated)));
                        else
                            new DatabaseComm(MainActivity.this, this, TAG_SYNC).execute(toJSON(PrescriptionLab.get(getApplicationContext()).getSynchronizable(UserLab.get(getApplicationContext()).getUserUUID(), lastUpdated, lastPrescription), this, lastUpdated, true));
                        notSynced = false;
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case TAG_PIN:
                    try {
                        if (output.getInt(TAG_SUCCESS) == 0)
                            showToast(R.string.database_error, getApplicationContext());
                        else
                            showToast(R.string.update_complete, getApplicationContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case TAG_SYNC:
                    try {
                        if (output.getInt(TAG_SUCCESS + "_sync_offline") == 1 && output.getInt(TAG_SUCCESS + "_prescription") == 1) {
                            if (output.getJSONArray(RegistrationFragment.TAG_RESULT).length() > 0) {
                                boolean result[] = PrescriptionLab.get(this).sync(this, output.getJSONArray(RegistrationFragment.TAG_RESULT), UserLab.get(this).getUserUUID());
                                if (result[0]) {
                                    Snackbar snackbar;
                                    if (result[1])
                                        snackbar = makeSnack("Doctor prescription(s) received!");
                                    else
                                        snackbar = makeSnack("Previously saved prescription(s) received!");
                                    snackbar.show();
                                    invalidateOptionsMenu();
                                }
                            }

                            long time = System.currentTimeMillis();
                            PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(UserLab.get(getApplicationContext()).getUsername() + "_lastUpdated", time).apply();
                            PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(UserLab.get(getApplicationContext()).getUsername() + "_lastPrescription", time).apply();
                        }
                    } catch (JSONException | ParseException | WriterException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else
            showToast(R.string.connection_failure, getApplicationContext());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && !checkState())
            showToast(R.string.version_warning, getApplicationContext());

        receiver = new NetworkChangedReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getApplicationContext().registerReceiver(receiver, intentFilter);
        if (getIntent().getAction() != null) {
            if (getIntent().getAction().equals(CustomNotificationService.ACTION_SHOW)) {
                String[] prescriptionsIDs = getIntent().getStringArrayExtra(CustomNotificationService.PRESCRIPTION_IDS);

                PrescriptionHandler.get().reset();
                PrescriptionHandler prescriptionHandler = PrescriptionHandler.get();
                List<CartMedicine> cartMedicines;

                for (String string : prescriptionsIDs) {
                    cartMedicines = PrescriptionLab.get(this).getCarts(UUID.fromString(string), System.currentTimeMillis());
                    for (CartMedicine cartMedicine : cartMedicines)
                        prescriptionHandler.addCart(cartMedicine);
                }
            }
        }
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.main_drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(drawerView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(drawerView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        View navigationHeaderView = navigationView.getHeaderView(0);

        TextView username = navigationHeaderView.findViewById(R.id.navigation_header_username);
        username.setText(UserLab.get(this).getUsername());
        TextView email = navigationHeaderView.findViewById(R.id.navigation_header_email);
        email.setText(UserLab.get(this).getEMail());

        int securityPIN = PreferenceManager.getDefaultSharedPreferences(this).getInt(UserLab.get(this).getUsername() + "_securityPin", 0);
        ((TextView) navigationView.getMenu().findItem(R.id.user_pin).getActionView()).setText(securityPIN == 0 ? null : String.valueOf(securityPIN));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (recentQRFlag) {
            getMenuInflater().inflate(R.menu.action_bar_items, menu);

            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(UserLab.get(this).getUsername(), false)) {
                menu.findItem(R.id.bar_recent_qr).setVisible(true);
            } else {
                menu.findItem(R.id.bar_recent_qr).setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.bar_recent_qr :
                Intent intent = QRActivity.newIntent(this);
                startActivity(intent);
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.main_fragment_container);
        switch (item.getItemId()) {
            case R.id.home_page:
                if (!(currentFragment instanceof SearchFragment)) {
                    fragmentManager.popBackStack("order_history", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentManager.popBackStack("regular_orders", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, new SearchFragment())
                            .addToBackStack("home_page")
                            .commit();
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.order_history :
                if (!(currentFragment instanceof OrderHistory)) {
                    fragmentManager.popBackStack("order_history", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, new OrderHistory())
                            .addToBackStack("order_history")
                            .commit();
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.regular_orders :
                if (!(currentFragment instanceof RegularOrders)) {
                    fragmentManager.popBackStack("regular_orders", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, new RegularOrders())
                            .addToBackStack("regular_orders")
                            .commit();
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.user_pin:
                if (checkState())
                    new OnlineTime(this, this).execute();
                else
                    showToast(R.string.update_required, getApplicationContext());
                break;
            case R.id.sign_out :
                FirebaseAuth.getInstance().signOut();
                PrescriptionHandler.get().reset();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isLoggedIn", false).apply();
                Intent intent = new Intent(this, AccessActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else if (getSupportFragmentManager().findFragmentById(R.id.main_fragment_container) instanceof SearchFragment)
            finish();
        else
            super.onBackPressed();
    }

    @Override
    public void lockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        recentQRFlag = false;
    }

    @Override
    public void unlockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        recentQRFlag = true;
    }

    @Override
    public void checkedNavigationItem(int item) {
        navigationView.getMenu().getItem(item).setChecked(true);
    }

    @Override
    public String setMenuPIN() {
        Integer securityPIN = new Random().nextInt(10000000) + 1000000;

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(UserLab.get(this).getUsername() + "_securityPin", securityPIN);
        editor.apply();

        TextView pinText = (TextView) navigationView.getMenu().findItem(R.id.user_pin).getActionView();
        pinText.setText(String.valueOf(securityPIN));
        return String.valueOf(securityPIN);
    }

    public static void showToast(int message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        ((TextView)(toast.getView()).findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        toast.show();
    }

    public static void showToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        ((TextView)(toast.getView()).findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        toast.show();
    }
}