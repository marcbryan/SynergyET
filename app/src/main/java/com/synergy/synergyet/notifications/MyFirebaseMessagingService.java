package com.synergy.synergyet.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
        // Guardamos el token en RealtimeDatabase
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

    /**
     * Actualiza el token en Realtime Database
     * @param refreshToken - El nuevo token
     */
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
        String sent = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY5);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
            //TODO: Enviar notificaciones en Android Oreo 8.0
            pushNotification(remoteMessage);
        }
    }

    /**
     * Muestra la notificación que hayamos recibido
     * @param remoteMessage - Clase que contiene los datos que se enviaron en el JSON
     */
    @SuppressWarnings("deprecation")
    private void pushNotification(RemoteMessage remoteMessage) {
        // Obtenemos los datos que se enviaron del JSON
        String user = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY1);
        String url = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY2);
        String body = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY3);
        String title = remoteMessage.getData().get(FirebaseStrings.REMOTE_MSG_KEY4);

        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtras.NOTIF_BUNDLE_STRING1, user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.synergy_logo_white)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        // Esto activa deprecated -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN (No hay otra manera de comprobarlo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            // Solo funcionará en Jelly Bean (Android 4.1 - API16) o versiones superiores
            // Notificación emergente (Heads-Up notification)
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i = 0;
        if (j > 0) {
            i = j;
        }
        final int i2 = i;

        if (url.equals(FirebaseStrings.DEFAULT_IMAGE_VALUE)) {
            // Ponemos la imagen por defecto en la notificación
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.google_user_icon);
            builder.setLargeIcon(largeIcon);
            // Mostrar notificación
            notificationManager.notify(i2, builder.build());
        } else {
            //TODO: Comprobar si se ve la foto en notificaciones
            Glide.with(getApplicationContext())
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Una vez cargada la imagen, la ponemos en el builder de la notificación
                        builder.setLargeIcon(resource);
                        // Mostrar notificación
                        notificationManager.notify(i2, builder.build());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        }
    }
}
