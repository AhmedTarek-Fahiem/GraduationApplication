package com.example.ahmed_tarek.graduationapplication;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.iq80.snappy.Main;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MainActivity extends SingleMedicineFragmentActivity implements AsyncResponse, NavigationView.OnNavigationItemSelectedListener, DrawerInterface {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView navigationView;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VERSION = "version";
    private static final String TAG_TIMESTAMP = "ver";
    private static final String TAG_MEDICINES = "medicines";

    private static boolean recentQRFlag = true;

    private static class LoadAll extends AsyncTask<String, String, JSONArray> {

        private AsyncResponse delegate = null;
        private ProgressDialog pDialog;
        private String type = null;

        LoadAll(AsyncResponse delegate, Activity activity) {
            pDialog = new ProgressDialog(activity);
            this.delegate = delegate;
        }

        private JSONObject makeHttpRequest(String url, String method, List<NameValuePair> params) {

            InputStream is = null;
            JSONObject jObj = null;
            String json = "";
            try {
                if (method.equals("POST")) {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                } else if (method.equals("GET")) {
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
            pDialog.setMessage("Checking for latest updates\nPlease wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONArray doInBackground(String... args) {
            JSONObject json = makeHttpRequest(args[0], "GET", new ArrayList<NameValuePair>());
            type = args[1];
            try {
                if (args[1].equals(TAG_MEDICINES) && json.getInt(TAG_SUCCESS) == 1)
                    return json.getJSONArray(TAG_MEDICINES);
                else if (args[1].equals(TAG_VERSION) && json.getInt(TAG_SUCCESS) == 1)
                    return json.getJSONArray(TAG_VERSION);
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            pDialog.dismiss();
            delegate.processFinish(result, type);
        }
    }

    public static class ChanceDialog extends DialogFragment {

        static ChanceDialog newInstance() {
            return new ChanceDialog();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.chance_dialog, container, false);

            Button yes = v.findViewById(R.id.yes);
            Button no = v.findViewById(R.id.no);
            yes.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET }, 123);
                    dismiss();
                }
            });
            no.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });

            return v;
        }
    }

    @Override
    protected Fragment createFragment() {
        return new MedicineListFragment();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)) {
            showToast(R.string.permission_warning, getApplicationContext());
            ChanceDialog.newInstance().show(getSupportFragmentManager().beginTransaction(), "dialog");
        }
        else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            if(checkState())
                new LoadAll(this, MainActivity.this).execute("http://ahmedgesraha.ddns.net/get_version.php", TAG_VERSION);
            else
                MainActivity.showToast(R.string.update_required, getApplicationContext());
        else
            showToast(R.string.permission_blocked, getApplicationContext());
    }

    private boolean checkState() {
        return ((ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || ((ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    public void processFinish(JSONArray output, String type) {
        if (type.equals(TAG_VERSION)) {
            if (output != null) {
                String version = null;
                try {
                    version = output.getJSONObject(0).getString(TAG_TIMESTAMP);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("database", false)) {
                    MedicineLab.get(this);
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("database", true).apply();
                    new LoadAll(this, MainActivity.this).execute("http://ahmedgesraha.ddns.net/get_medicines.php", TAG_MEDICINES);
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("version", version).apply();
                }
                else if (!version.equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("version", null))) {
                    new LoadAll(this, MainActivity.this).execute("http://ahmedgesraha.ddns.net/get_medicines.php", TAG_MEDICINES);
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("version", version).apply();
                }
                else {
                    MedicineLab.get(this);
                    showToast(R.string.already_upToDate, getApplicationContext());
                }
            } else
                showToast(R.string.connection_failure, getApplicationContext());
        }
        else if(type.equals(TAG_MEDICINES)) {
            if(output != null) {
                try {
                    MedicineLab.update(getApplicationContext(), output);
                } catch (ExecutionException | InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
            } else
                MainActivity.showToast(R.string.connection_failure, getApplicationContext());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().getAction() != null) {
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
        if (Build.VERSION.SDK_INT >= 23)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET}, 123);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (checkState())
                new LoadAll(this, MainActivity.this).execute("http://ahmedgesraha.ddns.net/get_version.php", TAG_VERSION);
            else
                MainActivity.showToast(R.string.update_required, getApplicationContext());
        } else
            MainActivity.showToast(R.string.no_permission, getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
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

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        View navigationHeaderView = navigationView.getHeaderView(0);

        TextView username = (TextView) navigationHeaderView.findViewById(R.id.navigation_header_username);
        username.setText(UserLab.get(this).getUsername());
        TextView email = (TextView) navigationHeaderView.findViewById(R.id.navigation_header_email);
        email.setText(UserLab.get(this).getEMail());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
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
                break;
            case R.id.order_history :
                if (!(currentFragment instanceof OrderHistory)) {
                    fragmentManager.popBackStack("order_history", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, new OrderHistory())
                            .addToBackStack("order_history")
                            .commit();
                }
                break;
            case R.id.regular_orders :
                if (!(currentFragment instanceof RegularOrders)) {
                    fragmentManager.popBackStack("regular_orders", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, new RegularOrders())
                            .addToBackStack("regular_orders")
                            .commit();
                }
                break;
            case R.id.sign_out :
                PrescriptionHandler.get().reset();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isLogin", false).apply();
                Intent intent = new Intent(this, AccessActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().findFragmentById(R.id.main_fragment_container) instanceof  MedicineListFragment) {
            finish();
        } else {
            super.onBackPressed();
        }
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

    public static void showToast(int message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        ((TextView)(toast.getView()).findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        toast.show();
    }
}