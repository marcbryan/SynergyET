package com.synergy.synergyet.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.synergy.synergyet.MessageActivity;
import com.synergy.synergyet.R;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        String sent = message.getData().get("sent");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
            sendNotification(message);
        }
        String refreshToken = message.getMessageId();
        System.out.println("msg id: "+refreshToken);
        //updateToken(refreshToken);
    }

    private void sendNotification(RemoteMessage message) {
        String user = message.getData().get(FirebaseStrings.REMOTE_MSG_KEY1);
        String icon = message.getData().get(FirebaseStrings.REMOTE_MSG_KEY2);
        String body = message.getData().get(FirebaseStrings.REMOTE_MSG_KEY3);
        String title = message.getData().get(FirebaseStrings.REMOTE_MSG_KEY4);
        String sent = message.getData().get(FirebaseStrings.REMOTE_MSG_KEY5);

        RemoteMessage.Notification notification = message.getNotification();
        int j = Integer.parseInt(user.replace("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtras.NOTIF_BUNDLE_STRING1, user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i = 0;
        if (j > 0) {
            i = j;
        }
        notificationManager.notify(i, builder.build());
    }

}
