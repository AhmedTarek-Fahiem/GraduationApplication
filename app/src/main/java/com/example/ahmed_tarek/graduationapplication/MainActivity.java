package com.example.ahmed_tarek.graduationapplication;

import android.content.Context;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends SingleMedicineFragmentActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerInterface {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private static boolean recentQRFlag = true;

    @Override
    protected Fragment createFragment() {
        return new MedicineListFragment();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().getAction() != null)
            if(getIntent().getAction().equals(CustomNotificationService.ACTION_SHOW)) {
                String[] a = getIntent().getStringArrayExtra(CustomNotificationService.PRESCRIPTION_IDS);
                for(String string : a)
                    Log.e("EUREKA","IT WORKS! " + UUID.fromString(string));
            }

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

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
}