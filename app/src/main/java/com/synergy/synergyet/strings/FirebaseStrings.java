package com.synergy.synergyet.strings;

/**
 * Clase con los diferentes textos de Cloud Firestore (colecciones, campos, etc.)
 */
public final class FirebaseStrings {

    // Colecciones de Cloud Firestore
    public static final String COLLECTION_1 = "users";
    public static final String COLLECTION_2 = "courses";
    public static final String COLLECTION_3 = "units";
    public static final String COLLECTION_4 = "tasks";

    // Campos
    public static final String FIELD1_C1 = "uid";
    public static final String FIELD1_C7 = "courses";
    public static final String FIELD1_C2 = "course_id";
    public static final String FIELD3_C2 = "category";
    public static final String FIELD6_C3 = "course_id";
    public static final String FIELD7_C3 = "order";
    public static final String FIELD1_C4 = "task_id";
    public static final String FIELD3_C4 = "unit_id";

    // Referencias Realtime Database
    public static final String REFERENCE_1 = "users";
    public static final String REFERENCE_2 = "conversations";
    public static final String REFERENCE_3 = "tokens";

    // Referencias Cloud Storage
    public static final String CLOUD_REFERENCE_1 = "profile_images";

    // Nodos Realtime Database
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
    public static final String REMOTE_MSG_KEY2 = "largeIcon";
    public static final String REMOTE_MSG_KEY3 = "body";
    public static final String REMOTE_MSG_KEY4 = "title";
    public static final String REMOTE_MSG_KEY5 = "sent";

    // Otros
    public static final String DEFAULT_USER_TYPE = "student";
    public static final String USER_TYPE_TEACHER = "teacher";
    public static final String DEFAULT_IMAGE_VALUE = "default";
    public static final String TASK_TYPE1 = "DELIVER";
    public static final String TASK_TYPE2 = "DOCUMENT";
    public static final String TASK_TYPE3 = "EXAM";

    /**
     * Constructor privado
     */
    private FirebaseStrings() {}
}
