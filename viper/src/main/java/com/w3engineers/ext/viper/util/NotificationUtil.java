package com.w3engineers.ext.viper.util;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.w3engineers.eth.util.helper.HandlerUtil;
import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.application.data.local.purchase.PurchaseConstants;
import com.w3engineers.ext.viper.application.ui.dataplan.DataPlanActivity;

public class NotificationUtil {

    private static final String CHANNEL_NAME = "meshChannel";
    private static final String CHANNEL_ID = "notification_channel";

    public static void showNotification(Context mContext, String title, String message) {
        HandlerUtil.postForeground(() -> {
            Intent intent = new Intent(mContext, DataPlanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder builder = getNotificationBuilder(mContext);
            builder.setContentIntent(pendingIntent);
            prepareNotification(mContext, builder, title, message);
            showNotification(mContext, builder, "100");
        });
    }

    public static void showSellerWarningNotification(Context mContext, String title, String message,int activeBuyerList) {
        HandlerUtil.postForeground(() -> {

            Intent intent = new Intent(mContext, DataPlanActivity.class);
            intent.putExtra(DataPlanActivity.class.getName(), true);
            intent.putExtra(PurchaseConstants.IntentKeys.NUMBER_OF_ACTIVE_BUYER, activeBuyerList);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder builder = getNotificationBuilder(mContext);

            //builder.addAction(0, "No", pendingIntent1);
            // builder.addAction(0, "Yes", pendingIntent2);
            builder.setContentIntent(pendingIntent);
            prepareNotification(mContext, builder, title, message);
            showNotification(mContext, builder, "seller_200");
        });
    }

    private static NotificationCompat.Builder getNotificationBuilder(Context context) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService
                    (Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            //channel.enableVibration(true);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    private static void prepareNotification(Context context, NotificationCompat.Builder builder, String title, String message) {
        builder.setWhen(System.currentTimeMillis())
                .setContentText(message)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setTicker(title)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND
                        | Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_launcher_mesh_rnd)
                .setPriority(NotificationManager.IMPORTANCE_HIGH);
    }

    private static void showNotification(Context context, NotificationCompat.Builder builder, String id) {
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        int notifyId = Math.abs(id.hashCode());
        if (notificationManager != null) {
            notificationManager.notify(notifyId, notification);
        }
    }

    public static void removeSellerNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        String id = "seller_200";
        int notifyId = Math.abs(id.hashCode());
        if (notificationManager != null) {
            notificationManager.cancel(notifyId);
        }
    }

}
