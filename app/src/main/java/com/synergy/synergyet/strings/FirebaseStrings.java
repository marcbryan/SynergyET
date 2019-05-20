package com.synergy.synergyet.strings;

/**
 * Clase con los diferentes textos de Cloud Firestore (colecciones, campos, etc.)
 */
public final class FirebaseStrings {

    // Colecciones de Cloud Firestore
    public static final String COLLECTION_1 = "users";
    public static final String COLLECTION_2 = "courses";
    public static final String COLLECTION_3 = "units";

    // Campos
    public static final String FIELD1_C1 = "uid";
    public static final String FIELD1_C7 = "courses";
    public static final String FIELD1_C2 = "course_id";
    public static final String FIELD3_C2 = "category";

    // Referencias RealtimeDatabase
    public static final String REFERENCE_1 = "users";
    public static final String REFERENCE_2 = "conversations";
    public static final String REFERENCE_3 = "tokens";

    // Nodos RealtimeDatabase
    public static final String KEY4_R1 = "conversations";

    public static final String KEY1_R2 = "lastMessageInfo";
    public static final String KEY2_R2 = "members";
    public static final String KEY3_R2 = "messages";

    public static final String K4_R1_CHILD1 = "unseenCount";
    public static final String K4_R1_CHILD2 = "imageURL";

    public static final String K1R2_CHILD1 = "lastMessage";
    public static final String K1R2_CHILD2 = "lastMessageDate";
    public static final String K1R2_CHILD3 = "lastMessageSender";

    // Campos propiedades notificaciones (Remote Message)
    public static final String REMOTE_MSG_KEY1 = "user";
    public static final String REMOTE_MSG_KEY2 = "icon";
    public static final String REMOTE_MSG_KEY3 = "body";
    public static final String REMOTE_MSG_KEY4 = "title";
    public static final String REMOTE_MSG_KEY5 = "sent";

    // Otros
    public static final String DEFAULT_USER_TYPE = "student";
    public static final String DEFAULT_IMAGE_VALUE = "default";

    /**
     * Constructor privado
     */
    private FirebaseStrings() {}
}
