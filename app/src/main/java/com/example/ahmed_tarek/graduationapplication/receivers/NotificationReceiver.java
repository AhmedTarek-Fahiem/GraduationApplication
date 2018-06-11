package com.example.ahmed_tarek.graduationapplication.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ahmed_tarek.graduationapplication.CustomNotificationService;

/**
 * Created by Rebel Ekko on 18/06/09.
 */

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_NOTIFY = "issue_notification";
    public static final String UUID = "prescription_id";
    public static final String FIRE_DATE = "fire_date";
    public static final int REQUEST_CODE = 1;
    public static final int HOUR = 8;

    @Override
    public void onReceive(Context context, Intent intent) {
        CustomNotificationService.enqueueWork(context, intent);
    }
}
