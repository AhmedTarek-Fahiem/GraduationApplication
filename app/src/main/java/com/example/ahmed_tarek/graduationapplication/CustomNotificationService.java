package com.example.ahmed_tarek.graduationapplication;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.ahmed_tarek.graduationapplication.receivers.BootUpReceiver;

import java.util.UUID;

/**
 * Created by cyber on 17/12/29.
 */

public class CustomNotificationService extends IntentService {

    public static final String ACTION_SHOW = "prescription_init";
    public static final String PRESCRIPTION_IDS = "prescription_ids";
    private static int notificationID = 0;

    public CustomNotificationService() {
        super(CustomNotificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String[] ids = RegularOrderLab.get(getApplicationContext()).getPrescriptionUUIDs(intent.getLongExtra(BootUpReceiver.DATE,0));
        long date = intent.getLongExtra(BootUpReceiver.DATE,0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if (intent.getAction().equals(BootUpReceiver.ACTION_NOTIFY)) {
            builder.setContentTitle("Medicines Reminder")
                    .setColor(getResources().getColor(R.color.colorAccent))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentText("You have pending regular medicines")
                    .setSmallIcon(R.drawable.ic_today_notification)
                    .setAutoCancel(true)
                    .setLights(Color.CYAN, 500, 2000)
                    .setContentIntent(PendingIntent.getActivity(this, notificationID, new Intent(this, MainActivity.class).putExtra(PRESCRIPTION_IDS, ids).setAction(ACTION_SHOW).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_UPDATE_CURRENT));
            final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(notificationID++, builder.build());
            for (String id : ids)
                RegularOrderLab.get(getApplicationContext()).removeEntry(UUID.fromString(id), date);
        }
    }
}