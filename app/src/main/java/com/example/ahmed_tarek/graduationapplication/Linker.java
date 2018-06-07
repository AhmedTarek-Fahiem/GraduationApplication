package com.example.ahmed_tarek.graduationapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by Rebel Ekko on 18/06/03.
 */

class Linker implements AsyncResponse{
    @SuppressLint("StaticFieldLeak")
    private static Linker sInstance;
    private Activity activity;
    private View view;

    static Linker getInstance(Activity activity, View view) {
        if (sInstance == null)
            sInstance = new Linker(activity, view);
        return sInstance;
    }

    private Linker(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
    }

    void syncOperation(boolean isVisible) throws JSONException {
        long lastUpdated = PreferenceManager.getDefaultSharedPreferences(activity).getLong(UserLab.get(activity).getUsername() + "_lastUpdated", 0), lastPrescription = PreferenceManager.getDefaultSharedPreferences(activity).getLong(UserLab.get(activity).getUsername() + "_lastPrescription", 0);
        String tag;
        if (isVisible)
            tag = MainActivity.TAG_SYNC_VISIBLE;
        else
            tag = MainActivity.TAG_SYNC;
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("database", false)) {
            if (lastUpdated == lastPrescription)
                new MainActivity.DatabaseComm(this, activity, tag).execute(new JSONObject().put("patient", new JSONObject().put("id", UserLab.get(activity).getUserUUID().toString())).put("lastUpdated", lastUpdated));
            else
                new MainActivity.DatabaseComm(this, activity, tag).execute(MainActivity.toJSON(PrescriptionLab.get(activity).getSynchronizable(UserLab.get(activity).getUserUUID(), lastUpdated, lastPrescription), activity, lastUpdated, true));
            MainActivity.notSynced = false;
        }
    }

    Snackbar makeSnack(int messageResource, final String cartMedicines) {
        final Snackbar snackbar = Snackbar
                .make(view, messageResource, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartMedicines != null)
                    activity.startActivity(QRActivity.newIntent(activity, cartMedicines));
                snackbar.dismiss();
            }
        });
        return snackbar;
    }

    @Override
    public void processFinish(JSONObject output, String type) {
        switch (type) {
            case MainActivity.TAG_SYNC:
            case MainActivity.TAG_SYNC_VISIBLE:
                try {
                    if (output.getInt(MainActivity.TAG_SUCCESS + "_sync_offline") == 1 && output.getInt(MainActivity.TAG_SUCCESS + "_prescription") == 1) {
                        if (output.getJSONArray(RegistrationFragment.TAG_RESULT).length() > 0) {
                            boolean result = PrescriptionLab.get(activity).sync(activity, output.getJSONArray(RegistrationFragment.TAG_RESULT), UserLab.get(activity).getUserUUID());
                            if (result)
                                makeSnack(R.string.doctor_prescriptions_received, null).show();
                            else
                                makeSnack(R.string.sync_complete, null).show();
                        }
                        if (type.equals(MainActivity.TAG_SYNC))
                            activity.invalidateOptionsMenu();
                        long time = System.currentTimeMillis();
                        PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong(UserLab.get(activity).getUsername() + "_lastUpdated", time).apply();
                        PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong(UserLab.get(activity).getUsername() + "_lastPrescription", time).apply();
                    }
                } catch (JSONException | ParseException | WriterException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
