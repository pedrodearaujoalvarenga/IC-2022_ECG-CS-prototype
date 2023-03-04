package com.cefetmg.september;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class testService extends Service {
    private MediaPlayer player;
    public testService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        player = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        player.setLooping(true);
        player.start();

        final String mypoggers = "ESP32";
        NotificationChannel notificationChannel = new NotificationChannel(mypoggers, mypoggers,
                NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
        Notification.Builder notification = new Notification.Builder(this, mypoggers)
                .setContentText("Status: Connected")
                .setContentTitle("ESP32-AD8232 Connection")
                .setSmallIcon(R.mipmap.ic_launcher);

        startForeground(1001, notification.build());

        return START_STICKY;

    }
}