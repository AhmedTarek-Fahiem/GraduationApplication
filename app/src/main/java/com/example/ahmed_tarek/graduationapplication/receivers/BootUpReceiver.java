package com.example.ahmed_tarek.graduationapplication.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ahmed_tarek.graduationapplication.Linker;
import com.example.ahmed_tarek.graduationapplication.RegularOrderLab;

/**
 * Created by cyber on 18/01/02.
 */

public final class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            long[] timestamps = RegularOrderLab.get(context).getTimeStamps(null);
            if (timestamps != null)
               for (long timeStamp : timestamps)
                    Linker.getInstance(null, null).initAlarm(context, timeStamp);
        }
    }
}