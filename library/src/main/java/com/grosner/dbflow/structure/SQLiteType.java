package com.grosner.dbflow.structure;

import java.util.HashMap;

/**
 * Author: andrewgrosner
 * Description: Holds the mapping between SQL data types and java classes.
 */
public enum SQLiteType {

    INTEGER,
    REAL,
    TEXT,
    BLOB;

    private static final HashMap<Class, SQLiteType> sTypeMap = new HashMap<Class, SQLiteType>() {
        {
            put(byte.class, SQLiteType.INTEGER);
            put(short.class, SQLiteType.INTEGER);
            put(int.class, SQLiteType.INTEGER);
            put(long.class, SQLiteType.INTEGER);
            put(float.class, SQLiteType.REAL);
            put(double.class, SQLiteType.REAL);
            put(boolean.class, SQLiteType.INTEGER);
            put(char.class, SQLiteType.TEXT);
            put(byte[].class, SQLiteType.BLOB);
            put(Byte.class, SQLiteType.INTEGER);
            put(Short.class, SQLiteType.INTEGER);
            put(Integer.class, SQLiteType.INTEGER);
            put(Long.class, SQLiteType.INTEGER);
            put(Float.class, SQLiteType.REAL);
            put(Double.class, SQLiteType.REAL);
            put(Boolean.class, SQLiteType.INTEGER);
            put(Character.class, SQLiteType.TEXT);
            put(String.class, SQLiteType.TEXT);
            put(Byte[].class, SQLiteType.BLOB);
        }
    };

    /**
     * Checks to see if the class is in this map
     *
     * @param clazz
     * @return true if the map has a class we can convert to an SQL type
     */
    public static boolean containsClass(Class clazz) {
        return sTypeMap.containsKey(clazz);
    }

    /**
     * Returns the {@link com.grosner.dbflow.structure.SQLiteType} for this class
     *
     * @param clazz
     * @return
     */
    public static SQLiteType get(Class clazz) {
        return sTypeMap.get(clazz);
    }
}
