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


    public static final String LINK = "http://ahmedgesraha.ddns.net/api/";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_PIN = "pin";
    public static final String TAG_VERSION = "version";
    public static final String TAG_TIMESTAMP = "ver";
    public static final String TAG_MEDICINES = "medicines";
    public static final String TAG_SYNC = "sync";
    public static final String TAG_PRESCRIPTION = "prescription";
    public static final String TAG_LOGIN = "login";
    public static final String TAG_REGISTRATION = "registration";
    public static final String TAG_CHECK = "check";



    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView navigationView;
    private static String version = null;
    private static boolean recentQRFlag = true;
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
                    new DatabaseComm(MainActivity.this, activity, TAG_VERSION).execute(new String[] { LINK + "getVersion" });
                    new DatabaseComm(MainActivity.this, activity, TAG_SYNC).execute(new String[] { LINK + "getPrescriptions", UserLab.get(activity).getUserUUID().toString() });

                } else
                    showToast(R.string.version_warning, getApplicationContext());
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

    static class DatabaseComm extends AsyncTask<String[], String, JSONObject> {

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
                case TAG_CHECK:
                    pDialog.setMessage("Checking data validity\nPlease wait...");
                    break;
                case TAG_REGISTRATION:
                    pDialog.setMessage("Checking and completing registration\nPlease wait...");
                    break;
                case TAG_PIN:
                    pDialog.setMessage("Updating PIN in database\nPlease wait...");
                    break;
                case TAG_MEDICINES:
                    pDialog.setMessage("Getting latest medicine updates\nPlease wait...");
                    break;
                case TAG_PRESCRIPTION:
                    pDialog.setMessage("Uploading data\nPlease wait...");
                    break;
                case VerificationFragment.TAG_STALL:
                    pDialog.setMessage("Verification successful\nRedirecting back to application\nPlease wait...");
                    break;
            }
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            if (!type.equals(TAG_VERSION) && !type.equals(TAG_SYNC))
                pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String[]... args) {
            JSONObject request = new JSONObject(), json;
            try {
                switch (type) {
                    case TAG_LOGIN:
                    case TAG_REGISTRATION:
                        request.put("username", args[0][1]);
                        request.put("password", args[0][2]);
                        if (type.equals(TAG_REGISTRATION)) {
                            request.put("email", args[0][3]);
                            request.put("dob", Long.valueOf(args[0][4]));
                            request.put("gender", args[0][5].equals("Male")?"m":"f");
                        }
                        json = makeHttpRequest(args[0][0], "POST", request);
                        break;
                    case TAG_CHECK:
                        request.put("username", args[0][1]);
                        request.put("email", args[0][2]);
                        json = makeHttpRequest(args[0][0], "POST", request);
                        break;
                    case TAG_PIN:
                        request.put("username", args[0][1]);
                        request.put("pin", args[0][2]);
                        json = makeHttpRequest(args[0][0], "POST", request);
                        break;
                    case TAG_SYNC:
                        request.put("id", args[0][1]);
                        json = makeHttpRequest(args[0][0], "POST", request);
                        break;
                    case TAG_PRESCRIPTION:
                        JSONObject prescriptionJson = new JSONObject();
                        prescriptionJson.put("id", args[0][1]);
                        prescriptionJson.put("prescription_date", Long.valueOf(args[0][2]));
                        prescriptionJson.put("price", Double.valueOf(args[0][3]));
                        prescriptionJson.put("patient_id", args[0][4]);
                        prescriptionJson.put("history_id", "1");
                        request.putOpt("prescription", prescriptionJson);
                        boolean case7 = false, case30 = false;
                        int index = Integer.parseInt(args[0][5]);

                        JSONArray cartMedicinesJsonArray = new JSONArray();
                        for (int i = 0; i < index * 3; i += 3) {
                            JSONObject cartMedicineJson = new JSONObject();
                            cartMedicineJson.put("medicine_id", args[1][i]);
                            cartMedicineJson.put("quantity", Integer.valueOf(args[1][i + 1]));
                            cartMedicineJson.put("repeat_duration", Integer.valueOf(args[1][i + 2]));
                            cartMedicineJson.put("prescription_id", args[0][1]);
                            cartMedicinesJsonArray.put(cartMedicineJson);
                            if (Integer.parseInt(args[1][i + 2]) == 7)
                                case7 = true;
                            else if (Integer.parseInt(args[1][i + 2]) == 30)
                                case30 = true;
                        }
                        request.putOpt("cartMedicines", cartMedicinesJsonArray);


                        JSONArray regularOrdersJsonArray = new JSONArray();
                        if (case7 && case30) {
                            JSONObject regularOrderJson = new JSONObject();
                            regularOrderJson.put("prescription_id", args[0][1]);
                            regularOrderJson.put("patient_id", args[0][4]);
                            regularOrderJson.put("fire_time", Long.valueOf(args[1][index * 3]));
                            regularOrdersJsonArray.put(regularOrderJson);
                            JSONObject regularOrderJson1 = new JSONObject();
                            regularOrderJson1.put("prescription_id", args[0][1]);
                            regularOrderJson1.put("patient_id", args[0][4]);
                            regularOrderJson1.put("fire_time", Long.valueOf(args[1][index * 3 + 1]));
                            regularOrdersJsonArray.put(regularOrderJson1);
                            request.putOpt("regularOrders", regularOrdersJsonArray);
                        } else if (case7 || case30) {
                            JSONObject regularOrderJson = new JSONObject();
                            regularOrderJson.put("prescription_id", args[0][1]);
                            regularOrderJson.put("patient_id", args[0][4]);
                            regularOrderJson.put("fire_time", Long.valueOf(args[1][index * 3]));
                            regularOrdersJsonArray.put(regularOrderJson);
                            request.putOpt("regularOrders", regularOrdersJsonArray);
                        }
                        json = makeHttpRequest(args[0][0], "POST", request);
                        break;
                    case VerificationFragment.TAG_STALL:
                        json = new JSONObject();
                        try {
                            Thread.sleep(2000);
                            json.put(TAG_SUCCESS, 1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        json = makeHttpRequest(args[0][0], "GET", request);
                        break;
                }
                if (type.equals(TAG_MEDICINES) && json.getInt(TAG_SUCCESS) == 1)
                    return json;
                else if (type.equals(TAG_VERSION) && json.getInt(TAG_SUCCESS) == 1)
                    return json;
                else if (type.equals(TAG_CHECK) || type.equals(TAG_REGISTRATION))
                    return json;
                else if (type.equals(TAG_LOGIN))
                    return json;
                else if (type.equals(TAG_PIN) || type.equals(TAG_PRESCRIPTION))
                    return json;
                else if (type.equals(TAG_SYNC) && ((json.getInt(TAG_SUCCESS + "_cart") == 1 && json.getInt(TAG_SUCCESS + "_prescription") == 1)))
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
            String PIN = setMenuPIN();
            new DatabaseComm(this, MainActivity.this, TAG_PIN).execute(new String[] { LINK + "setPIN", UserLab.get(this).getUsername(), PIN });
            PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(UserLab.get(this).getUsername() + "_timeOut", time).apply();
        }
    }

    @Override
    protected Fragment createFragment() {
        return new MedicineListFragment();
    }

    private boolean checkState() {
        return ((ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || ((ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
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
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("database", true).apply();
                            new DatabaseComm(this, MainActivity.this, TAG_MEDICINES).execute(new String[] { MainActivity.LINK + "getMedicinesList" });
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("version", version).apply();
                        } else if (!version.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("version", null))) {
                            new DatabaseComm(MainActivity.this, MainActivity.this, TAG_MEDICINES).execute(new String[] { MainActivity.LINK + "getMedicinesList" });
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("version", version).apply();
                        }
                    } else
                        showToast(R.string.update_required, getApplicationContext());
                    break;
                case TAG_MEDICINES:
                    try {
                        MedicineLab.get(this).update(getApplicationContext(), output.getJSONArray(TAG_MEDICINES));
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
                        if (!output.getJSONArray(RegistrationFragment.TAG_RESULT).isNull(0))
                            PrescriptionLab.get(this).sync(this, output.getJSONArray(RegistrationFragment.TAG_RESULT), UserLab.get(this).getUserUUID());
                        //showToast(R.string.sync_complete, getApplicationContext());
                    } catch (JSONException | ParseException e) {
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
                if (!(currentFragment instanceof MedicineListFragment)) {
                    fragmentManager.popBackStack("order_history", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentManager.popBackStack("regular_orders", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, new MedicineListFragment())
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
        else if (getSupportFragmentManager().findFragmentById(R.id.main_fragment_container) instanceof  MedicineListFragment)
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