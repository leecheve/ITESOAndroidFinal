package garden.iteso.com.proyectoalarma;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by luisneto on 5/10/2015.
 */
public class GcmIntentService extends IntentService {
    private Ringtone ringtone;

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                // If it's a regular GCM message, do some work.
                sendNotification("Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification(getResources().getString(R.string.text_notification));
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String stringValue = preferences.getString("alerts_ringtone", "");

        boolean doFlash = preferences.getBoolean("alert_flash_checkbox", false);
        boolean doVibrate = preferences.getBoolean("alert_vibrate_checkbox", false);

        long[] vibPattern = {0, 100, 100, 100, 100, 100, 100, 100, 100, 500};

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.title_notification))
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setSound(Uri.parse(stringValue), RingtoneManager.TYPE_ALARM);

        if (doFlash)
            mBuilder.setLights(Color.RED, 50, 50);

        if (doVibrate)
            mBuilder.setVibrate(vibPattern);

        mBuilder.setContentIntent(contentIntent);

        Notification mNotification = mBuilder.build();
        mNotification.flags |= Notification.FLAG_INSISTENT;

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
}