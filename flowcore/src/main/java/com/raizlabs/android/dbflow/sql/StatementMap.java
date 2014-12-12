package com.raizlabs.android.dbflow.sql;

import java.util.HashMap;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
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
