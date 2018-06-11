package com.example.ahmed_tarek.graduationapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;

import com.example.ahmed_tarek.graduationapplication.receivers.NotificationReceiver;

import java.util.UUID;

/**
 * Created by cyber on 17/12/29.
 */

public class CustomNotificationService extends JobIntentService {

    public static final String CHANNEL_ID = "notification_channel";
    public static final String VISIBLE_ID = "Medicines Reminder";
    public static final String ACTION_SHOW = "prescription_init";
    public static final String PRESCRIPTION_IDS = "prescription_ids";
    private static int notificationID = 0;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CustomNotificationService.class, 1, work);
    }

    @Override
    protected void onHandleWork(@Nullable Intent intent) {
        String[] ids = RegularOrderLab.get(getApplicationContext()).getPrescriptionUUIDs(intent.getLongExtra(NotificationReceiver.DATE,0));
        long date = intent.getLongExtra(NotificationReceiver.DATE,0);
        final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CustomNotificationService.CHANNEL_ID, CustomNotificationService.VISIBLE_ID, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Displays reminders for regular medicines");
            manager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else
            builder = new Notification.Builder(this);

        if (intent.getAction().equals(NotificationReceiver.ACTION_NOTIFY)) {
            builder.setContentTitle("Medicines Reminder")
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentText("You have pending regular medicines")
                    .setSmallIcon(R.drawable.ic_today_notification)
                    .setAutoCancel(true)
                    .setLights(Color.CYAN, 500, 2000)
                    .setContentIntent(PendingIntent.getActivity(this, notificationID, new Intent(this, MainActivity.class).putExtra(PRESCRIPTION_IDS, ids).setAction(ACTION_SHOW).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_UPDATE_CURRENT))
                    .setPriority(Notification.PRIORITY_DEFAULT);

            manager.notify(notificationID++, builder.build());
            for (String id : ids)
                RegularOrderLab.get(getApplicationContext()).removeEntry(UUID.fromString(id), date);
        }
    }
}