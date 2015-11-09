package com.raizlabs.android.dbflow.sql;

import com.raizlabs.android.dbflow.data.Blob;

import java.util.Date;
import java.util.HashMap;

/**
 * Author: andrewgrosner
 * Description: Holds the mapping between SQL data types and java classes.
 */
public enum SQLiteType {

    INTEGER,
    REAL,
    TEXT,
    BLOB,
    DATE;

    private static final HashMap<String, SQLiteType> sTypeMap = new HashMap<String, SQLiteType>() {
        {
            put(byte.class.getName(), SQLiteType.INTEGER);
            put(short.class.getName(), SQLiteType.INTEGER);
            put(int.class.getName(), SQLiteType.INTEGER);
            put(long.class.getName(), SQLiteType.INTEGER);
            put(float.class.getName(), SQLiteType.REAL);
            put(double.class.getName(), SQLiteType.REAL);
            put(boolean.class.getName(), SQLiteType.INTEGER);
            put(char.class.getName(), SQLiteType.TEXT);
            put(byte[].class.getName(), SQLiteType.BLOB);
            put(Byte.class.getName(), SQLiteType.INTEGER);
            put(Short.class.getName(), SQLiteType.INTEGER);
            put(Integer.class.getName(), SQLiteType.INTEGER);
            put(Long.class.getName(), SQLiteType.INTEGER);
            put(Float.class.getName(), SQLiteType.REAL);
            put(Double.class.getName(), SQLiteType.REAL);
            put(Boolean.class.getName(), SQLiteType.INTEGER);
            put(Character.class.getName(), SQLiteType.TEXT);
            put(String.class.getName(), SQLiteType.TEXT);
            put(Byte[].class.getName(), SQLiteType.BLOB);
            put(Blob.class.getName(), SQLiteType.BLOB);
            put(Date.class.getName(), SQLiteType.DATE);
        }
    };

    /**
     * Returns the {@link SQLiteType} for this class
     *
     * @param className The fully qualified class name
     * @return The type from the class name
     */
    public static SQLiteType get(String className) {
        return sTypeMap.get(className);
    }

    public static boolean containsClass(String className) {
        return sTypeMap.containsKey(className);
    }
}
