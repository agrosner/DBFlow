package com.raizlabs.android.dbflow.processor;

import java.util.HashMap;

/**
 * Description: Holds the methods to write for a specific {@link com.raizlabs.android.dbflow.sql.SQLiteType}
 * by appending bindTo[method].
 */
public class StatementMap {

    private static final HashMap<SQLiteType, String> mMap = new HashMap<SQLiteType, String>() {
        {
            put(SQLiteType.INTEGER, "Long");
            put(SQLiteType.BLOB, "Blob");
            put(SQLiteType.REAL, "Double");
            put(SQLiteType.TEXT, "String");
        }

    };

    public static String getStatement(SQLiteType sqLiteType) {
        return mMap.get(sqLiteType);
    }
}
