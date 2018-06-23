package com.example.ahmed_tarek.graduationapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.example.ahmed_tarek.graduationapplication.receivers.NotificationReceiver;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.TimeZone;

/**
 * Created by Rebel Ekko on 18/06/03.
 */

public class Linker implements AsyncResponse{

    @SuppressLint("StaticFieldLeak")
    private static Linker sInstance;
    private Activity activity;
    private View view;

    public static Linker getInstance(Activity activity, View view) {
        if (sInstance == null)
            sInstance = new Linker(activity, view);
        else {
            if (activity != null)
                sInstance.activity = activity;
            if (view != null)
                sInstance.view = view;
        }
        return sInstance;
    }

    private Linker(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
    }

    void syncOperation(boolean isVisible) throws JSONException {
        long lastUpdated = PreferenceManager.getDefaultSharedPreferences(activity).getLong(UserLab.get(activity).getUsername() + "_lastUpdated", 0), lastPrescription = PreferenceManager.getDefaultSharedPreferences(activity).getLong(UserLab.get(activity).getUsername() + "_lastPrescription", 0);
        String tag;
        Log.e("TIME", lastPrescription + " " + lastUpdated);
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

    private Snackbar makeSnack(int messageResource) {
        final Snackbar snackbar = Snackbar
                .make(view, messageResource, Snackbar.LENGTH_INDEFINITE);
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
        switch (type) {
            case MainActivity.TAG_SYNC:
            case MainActivity.TAG_SYNC_VISIBLE:
                try {
                    if (output.getInt(MainActivity.TAG_SUCCESS + "_sync_offline") == 1 && output.getInt(MainActivity.TAG_SUCCESS + "_prescription") == 1) {
                        if (output.getJSONArray(RegistrationFragment.TAG_RESULT).length() > 0) {
                            boolean result = PrescriptionLab.get(activity).sync(activity, output.getJSONArray(RegistrationFragment.TAG_RESULT), UserLab.get(activity).getUserUUID());
                            if (result) {
                                makeSnack(R.string.doctor_prescriptions_received).show();
                                PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(UserLab.get(activity).getUsername() + "_isDoctorPrescription", true).apply();
                            } else {
                                makeSnack(R.string.sync_complete).show();
                                PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(UserLab.get(activity).getUsername() + "_isDoctorPrescription", false).apply();
                            }
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

    private long fireAfter(int days) {
        return AlarmManager.INTERVAL_DAY - (TimeZone.getDefault().getOffset(System.currentTimeMillis()) + System.currentTimeMillis()) % AlarmManager.INTERVAL_DAY + days * AlarmManager.INTERVAL_DAY + NotificationReceiver.HOUR * AlarmManager.INTERVAL_HOUR + System.currentTimeMillis();
    }

    public void initAlarm(Context context, long fireAt) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, fireAt, PendingIntent.getBroadcast(context, NotificationReceiver.REQUEST_CODE, new Intent(context, NotificationReceiver.class).setAction(NotificationReceiver.ACTION_NOTIFY).putExtra(NotificationReceiver.FIRE_DATE, fireAt).addCategory("" + fireAt), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void cancelAlarm(Context context, long timeStamp) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NotificationReceiver.REQUEST_CODE, new Intent(context, NotificationReceiver.class).setAction(NotificationReceiver.ACTION_NOTIFY).addCategory("" + timeStamp), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    void schedule(Context context, java.util.UUID id) {
        boolean case7 = false, case30 = false;
        long time;
        for (CartMedicine cartMedicine : PrescriptionLab.get(context).getCarts(id)) {
            switch (cartMedicine.getRepeatDuration()) {
                case 7:
                    case7 = true;
                    break;
                case 30:
                    case30 = true;
                    break;
            }
        }
        if (case7) {
            time = fireAfter(6);
            if (!RegularOrderLab.get(context).reminderExists(time))
                initAlarm(context, time);
            RegularOrderLab.get(context).addRegularOrder(id, time);
        }
        if (case30) {
            time = fireAfter(29);
            if (!RegularOrderLab.get(context).reminderExists(time))
                initAlarm(context, time);
            RegularOrderLab.get(context).addRegularOrder(id, time);
        }
    }

    boolean checkState(Context context) {
        return ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED || ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }
}
