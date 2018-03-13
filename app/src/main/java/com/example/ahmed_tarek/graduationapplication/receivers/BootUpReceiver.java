package com.example.ahmed_tarek.graduationapplication.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ahmed_tarek.graduationapplication.CartMedicine;
import com.example.ahmed_tarek.graduationapplication.CustomNotificationService;
import com.example.ahmed_tarek.graduationapplication.PrescriptionLab;
import com.example.ahmed_tarek.graduationapplication.RegularOrderLab;
import java.util.TimeZone;

/**
 * Created by cyber on 18/01/02.
 */

public final class BootUpReceiver extends BroadcastReceiver {

    public static final String ACTION_NOTIFY = "issue_notification";
    public static final String UUID = "prescription_id";
    public static final String DATE = "fire_date";
    private static final int REQUEST_CODE = 1;
    private static final int HOUR = 8;

    public static long fireAfter(int days) {
        return AlarmManager.INTERVAL_DAY - (TimeZone.getDefault().getOffset(System.currentTimeMillis()) + System.currentTimeMillis()) % AlarmManager.INTERVAL_DAY + days * AlarmManager.INTERVAL_DAY + HOUR * AlarmManager.INTERVAL_HOUR + System.currentTimeMillis();
    }

    public static void alarmInit(Context context, long fireAt) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, fireAt, PendingIntent.getService(context, REQUEST_CODE, new Intent(context, CustomNotificationService.class).setAction(ACTION_NOTIFY).putExtra(DATE, fireAt).addCategory("" + fireAt), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void cancelAlarm(Context context, long timeStamp) {
        PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE, new Intent(context, CustomNotificationService.class).setAction(ACTION_NOTIFY).addCategory("" + timeStamp), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static void schedule(Context context, java.util.UUID id) {
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
//            time = System.currentTimeMillis() + 60000;
            if (!RegularOrderLab.get(context).reminderExists(time))
                alarmInit(context, time);
            RegularOrderLab.get(context).addRegularOrder(id, time);
        }
        if (case30) {
            time = fireAfter(29);
//            time = System.currentTimeMillis() + 120000;
            if (!RegularOrderLab.get(context).reminderExists(time))
                alarmInit(context, time);
            RegularOrderLab.get(context).addRegularOrder(id, time);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            if (RegularOrderLab.get(context).getTimeStamps() != null)
                for (long timeStamp : RegularOrderLab.get(context).getTimeStamps())
                    alarmInit(context, timeStamp);
    }
}