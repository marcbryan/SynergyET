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

    // Otros
    public static final String DEFAULT_USER_TYPE = "student";
    public static final String DEFAULT_IMAGE_VALUE = "default";

    /**
     * Constructor privado
     */
    private FirebaseStrings() {}
}
