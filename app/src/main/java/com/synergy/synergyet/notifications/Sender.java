package com.synergy.synergyet.notifications;

public class Sender {
    // Información sobre Sintaxis de mensajes HTTP descendentes (JSON) -> https://firebase.google.com/docs/cloud-messaging/http-server-ref

    // Este parámetro especifica los pares clave-valor predefinidos visibles para el usuario de la carga útil de notificación
    public Data data;
    //Este parámetro especifica el destinatario de un mensaje
    public String to;

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }
}
