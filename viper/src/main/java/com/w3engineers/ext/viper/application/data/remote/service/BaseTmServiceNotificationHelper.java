package com.w3engineers.ext.viper.application.data.remote.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.w3engineers.ext.strom.util.helper.NotificationUtil;
import com.w3engineers.ext.viper.R;

/*
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 */
public class BaseTmServiceNotificationHelper {

    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_STOP_SERVICE = "stop_service";
    private Service mService;

    public BaseTmServiceNotificationHelper(Service service) {

        this.mService = service;

    }


    /**
     * Upon checking the source code icon texts can be configured from app layer. App Name should
     * be automatically fetched from Application layer if developers maintain convention
     */
    public void startForegroundService() {

        if(mService == null) {
            return;
        }

        String notificationTitle = (String.format("%s is running", mService.getString(R.string.app_name)));
        String bigStyleTextTitle = notificationTitle + " as a background service.";
        String notificationBody = String.format("Expand to stop %s", mService.getString(R.string.app_name));
        String stopServiceText = "Stop Service";
        String bigTextStopService = String.format("You can stop the service by clicking on '%s' button.",
                stopServiceText);

        // Make notification show big text.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(bigStyleTextTitle);
        bigTextStyle.bigText(bigTextStopService);

        Bitmap largeIconBitmap = BitmapFactory.decodeResource(mService.getResources(), R.drawable.ic_rm_notif_icon_black_24dp);

        // Add Play button intent in notification.
        Intent stopServiceIntent = new Intent(mService, mService.getClass());
        stopServiceIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent pendingStopServiceIntent = PendingIntent.getService(mService, 0, stopServiceIntent, 0);
        NotificationCompat.Action stopServiceAction = new NotificationCompat.Action
                (R.drawable.ic_rm_off_black_24dp, stopServiceText, pendingStopServiceIntent);

        // Create notification builder.
        NotificationCompat.Builder builder = NotificationUtil.getBuilder(mService);
        builder.setContentTitle(notificationTitle).setWhen(System.currentTimeMillis()).
                setSmallIcon(R.drawable.ic_rm_notif_icon_black_24dp).setLargeIcon(largeIconBitmap).
                addAction(stopServiceAction).setStyle(bigTextStyle).
                setContentText(notificationBody).
                setAutoCancel(false);//This one can be true as Activity Pause/Resume would re appear
        // the notification normally unless developer modify the behavior

        // Build the notification.
        Notification notification = builder.build();

        // Start foreground service.
        mService.startForeground(NOTIFICATION_ID, notification);
    }

    public void stopForegroundService() {
        // Stop foreground service and remove the notification.
        mService.stopForeground(true);

    }

}
