package watcharaphans.bitcombine.co.th.bitcamera.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.Random;

import watcharaphans.bitcombine.co.th.bitcamera.MainActivity;
import watcharaphans.bitcombine.co.th.bitcamera.R;
import watcharaphans.bitcombine.co.th.bitcamera.StartActivityOnBootReceiver;

/**
 * Created by Jaison on 09/11/16.
 */

public class NotificationHelper extends ContextWrapper {

    private static final String TAG = "ServiceDemo";
    public static String POSITIVE_CLICK = "POSITIVE_CLICK";
    public static String NEGATIVE_CLICK = "NEGATIVE_CLICK";
    public static String GROUP_KEY = "GROUP_KEY";

    private NotificationManager manager;
    public static final String PRIMARY_CHANNEL = "default";
    public static final String SECONDARY_CHANNEL = "second";

    public NotificationHelper(Context ctx) {
        super(ctx);

        NotificationChannel chan1 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan1 = new NotificationChannel(PRIMARY_CHANNEL,
                    getString(R.string.noti_channel_default), NotificationManager.IMPORTANCE_DEFAULT);

            chan1.setLightColor(Color.GREEN);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(chan1);

            NotificationChannel chan2 = new NotificationChannel(SECONDARY_CHANNEL,
                    getString(R.string.noti_channel_second), NotificationManager.IMPORTANCE_HIGH);
            chan2.setLightColor(Color.BLUE);
            chan2.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(chan2);
        }
    }

    public Notification.Builder getNotification2(String title, String body) {

        Log.d("ServiceDemo","getNotification2 " + title + " body : "+ body);

        int notificationId = new Random().nextInt();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent positive = new Intent(this, StartActivityOnBootReceiver.class);
        positive.putExtra("notiID", notificationId);
        positive.setAction(POSITIVE_CLICK);

        PendingIntent pIntent_positive = PendingIntent.getBroadcast(this, notificationId, positive, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent negative = new Intent(this, StartActivityOnBootReceiver.class);
        negative.putExtra("notiID", notificationId);
        negative.setAction(NEGATIVE_CLICK);

        PendingIntent pIntent_negative = PendingIntent.getBroadcast(this, notificationId, negative, PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notifications);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(getApplicationContext(), SECONDARY_CHANNEL)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setStyle(new Notification.BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title)
                    .setSummaryText("Summary Text"))
                    .setColor(Color.BLUE)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .addAction(R.mipmap.ic_launcher,"ตกลง",pIntent_negative)
                    .setAutoCancel(true);
        }else{
            return null;
        }

    }


    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

    private int getSmallIcon() {
        return android.R.drawable.stat_notify_chat;
    }


    private NotificationManager getManager() {
        if (manager == null) {

            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public void showHeadsUpNotification() {

        Log.d(TAG, "showHeadsUpNotification..");
        int notificationId = new Random().nextInt();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent positive = new Intent(this, StartActivityOnBootReceiver.class);
        positive.putExtra("notiID", notificationId);
        positive.setAction(POSITIVE_CLICK);

        PendingIntent pIntent_positive = PendingIntent.getBroadcast(this, notificationId, positive, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent negative = new Intent(this, StartActivityOnBootReceiver.class);
        negative.putExtra("notiID", notificationId);
        negative.setAction(NEGATIVE_CLICK);

        PendingIntent pIntent_negative = PendingIntent.getBroadcast(this, notificationId, negative, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notification = NotificationHelper.createNotificationBuider(this,
                "แจ้งเตือน", "ทำการส่งรูปต่อ", R.drawable.ic_notifications, pIntent);

        notification.setPriority(Notification.PRIORITY_HIGH).setVibrate(new long[0]); // แสดง HeadsUp noti
        notification.setColor(Color.BLUE);
        notification.addAction(new NotificationCompat.Action(R.drawable.ic_notifications, "ตกลง", pIntent_positive));

        NotificationHelper.showNotification(this, notificationId, notification.build());

    }


    public static NotificationCompat.Builder createNotificationBuider(Context context, String title,
                                                                      String message, int smallIcon) {
        return createNotificationBuider(context, title, message, smallIcon, null, 0, true, null);
    }

    public static NotificationCompat.Builder createNotificationBuider(Context context, String title,
                                                                      String message, int smallIcon, PendingIntent contentIntent) {
        return createNotificationBuider(context, title, message, smallIcon, null, 0, true, contentIntent);
    }

    public static NotificationCompat.Builder createNotificationBuider(Context context, String title, String message,
                                                                      int smallIcon, Bitmap largeIcon, int colorID,
                                                                      boolean isAutoCancel, PendingIntent contentIntent) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setAutoCancel(isAutoCancel);
            builder.setGroup(GROUP_KEY);

            if (!TextUtils.isEmpty(title))
                builder.setContentTitle(title);
            if (!TextUtils.isEmpty(message))
                builder.setContentText(message);
            if (smallIcon != 0)
                builder.setSmallIcon(smallIcon);
            if (largeIcon != null)
                builder.setLargeIcon(largeIcon);
            if (colorID != 0)
                builder.setColor(ContextCompat.getColor(context, colorID));
            if (contentIntent != null)
                builder.setContentIntent(contentIntent);

            return builder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void showNotification(Context context, int notiID, Notification notification) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notiID, notification);
    }


}
