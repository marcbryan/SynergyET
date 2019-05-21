package com.synergy.synergyet.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.synergy.synergyet.MessageActivity;
import com.synergy.synergyet.R;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // onNewToken() -> Sustituye a FirebaseIdService (está deprecated)
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                if (firebaseUser != null) {
                    String refreshToken = instanceIdResult.getToken();
                    updateToken(refreshToken);
                }
            }
        });
    }

    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Añadimos el token a Realtime Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_3);
        Token token = new Token(refreshToken);
        reference.child(firebaseUser.getUid()).setValue(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String sent = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY6);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
            sendNotification(remoteMessage);
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY1);
        String icon = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY2);
        String url = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY3);
        String body = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY4);
        String title = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY5);

        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
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
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            // Solo funcionará en Jelly Bean (Android 4.1 - API16) o versiones superiores
            // Notificación emergente (Heads-Up notification)
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i = 0;
        if (j > 0) {
            i = j;
        }

        //TODO: Set large icon (para mostrar foto del usuario en notificación)

        notificationManager.notify(i, builder.build());
    }

}
