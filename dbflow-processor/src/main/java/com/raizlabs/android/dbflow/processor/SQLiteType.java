package com.raizlabs.android.dbflow.processor;

import com.raizlabs.android.dbflow.data.Blob;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: Holds the mapping between SQL data types and java classes.
 */
public enum SQLiteType {

    INTEGER {
        @Override
        public String getSQLiteStatementMethod() {
            return "Long";
        }
    },
    REAL {
        @Override
        public String getSQLiteStatementMethod() {
            return "Double";
        }
    },
    TEXT {
        @Override
        public String getSQLiteStatementMethod() {
            return "String";
        }
    },
    BLOB {
        @Override
        public String getSQLiteStatementMethod() {
            return "Blob";
        }
    };

    public abstract String getSQLiteStatementMethod();

    private static final HashMap<TypeName, SQLiteType> sTypeMap = new HashMap<TypeName, SQLiteType>() {
        {
            put(TypeName.BYTE, SQLiteType.INTEGER);
            put(TypeName.SHORT, SQLiteType.INTEGER);
            put(TypeName.INT, SQLiteType.INTEGER);
            put(TypeName.LONG, SQLiteType.INTEGER);
            put(TypeName.FLOAT, SQLiteType.REAL);
            put(TypeName.DOUBLE, SQLiteType.REAL);
            put(TypeName.BOOLEAN, SQLiteType.INTEGER);
            put(TypeName.CHAR, SQLiteType.TEXT);
            put(ArrayTypeName.of(TypeName.BYTE), SQLiteType.BLOB);
            put(TypeName.BYTE.box(), SQLiteType.INTEGER);
            put(TypeName.SHORT.box(), SQLiteType.INTEGER);
            put(TypeName.INT.box(), SQLiteType.INTEGER);
            put(TypeName.LONG.box(), SQLiteType.INTEGER);
            put(TypeName.FLOAT.box(), SQLiteType.REAL);
            put(TypeName.DOUBLE.box(), SQLiteType.REAL);
            put(TypeName.BOOLEAN.box(), SQLiteType.INTEGER);
            put(TypeName.CHAR.box(), SQLiteType.TEXT);
            put(ClassName.get(String.class), SQLiteType.TEXT);
            put(ArrayTypeName.of(TypeName.BYTE.box()), SQLiteType.BLOB);
        }
    };

    public static SQLiteType get(TypeName typeName) {
        SQLiteType sqLiteType = sTypeMap.get(typeName);

        // fix for enums
        if (sqLiteType == null) {
            sqLiteType = SQLiteType.TEXT;
        }
        return sqLiteType;
    }

    public static boolean containsType(TypeName typeName) {
        return sTypeMap.containsKey(typeName);
    }

    private static final Map<TypeName, String> sMethodMap = new HashMap<TypeName, String>() {{
        put(ArrayTypeName.of(TypeName.BYTE), "getBlob");
        put(ArrayTypeName.of(TypeName.BYTE.box()), "getBlob");
        put(TypeName.DOUBLE, "getDouble");
        put(TypeName.DOUBLE.box(), "getDouble");
        put(TypeName.FLOAT, "getFloat");
        put(TypeName.FLOAT.box(), "getFloat");
        put(TypeName.INT, "getInt");
        put(TypeName.INT.box(), "getInt");
        put(TypeName.LONG, "getLong");
        put(TypeName.LONG.box(), "getLong");
        put(TypeName.SHORT, "getShort");
        put(TypeName.SHORT.box(), "getShort");
        put(ClassName.get(String.class), "getString");
        put(ClassName.get(Blob.class), "getBlob");
    }};

    public static boolean containsMethod(TypeName typeName) {
        return sMethodMap.containsKey(typeName);
    }

    public static String getMethod(TypeName typeName) {
        return sMethodMap.get(typeName);
    }

    private static Map<TypeName, String> sModelContainerMethodMap = new HashMap<TypeName, String>() {{
        put(ArrayTypeName.of(TypeName.BYTE), "getBlb");
        put(ArrayTypeName.of(TypeName.BYTE.box()), "getBlob");
        put(TypeName.DOUBLE, "getDble");
        put(TypeName.DOUBLE.box(), "getDouble");
        put(TypeName.FLOAT, "getFlt");
        put(TypeName.FLOAT.box(), "getFloat");
        put(TypeName.INT, "getInt");
        put(TypeName.INT.box(), "getInteger");
        put(TypeName.LONG, "getLng");
        put(TypeName.LONG.box(), "getLong");
        put(TypeName.SHORT, "getShrt");
        put(TypeName.SHORT.box(), "getShort");
        put(TypeName.BOOLEAN.box(), "getBoolean");
        put(TypeName.BOOLEAN, "getBool");
        put(ClassName.get(String.class), "getString");
        put(ClassName.get(Blob.class), "getBlb");
    }};

    public static String getModelContainerMethod(TypeName typeName) {
        return sModelContainerMethodMap.get(typeName);
    }

}
