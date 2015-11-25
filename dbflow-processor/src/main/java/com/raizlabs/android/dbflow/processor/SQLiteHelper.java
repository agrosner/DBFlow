package com.raizlabs.android.dbflow.processor;

import com.raizlabs.android.dbflow.data.Blob;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Description: Holds the mapping between SQL data types and java classes used in the processor.
 */
public enum SQLiteHelper {

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

    private static final HashMap<TypeName, SQLiteHelper> sTypeMap = new HashMap<TypeName, SQLiteHelper>() {
        {
            put(TypeName.BYTE, SQLiteHelper.INTEGER);
            put(TypeName.SHORT, SQLiteHelper.INTEGER);
            put(TypeName.INT, SQLiteHelper.INTEGER);
            put(TypeName.LONG, SQLiteHelper.INTEGER);
            put(TypeName.FLOAT, SQLiteHelper.REAL);
            put(TypeName.DOUBLE, SQLiteHelper.REAL);
            put(TypeName.BOOLEAN, SQLiteHelper.INTEGER);
            put(TypeName.CHAR, SQLiteHelper.TEXT);
            put(ArrayTypeName.of(TypeName.BYTE), SQLiteHelper.BLOB);
            put(TypeName.BYTE.box(), SQLiteHelper.INTEGER);
            put(TypeName.SHORT.box(), SQLiteHelper.INTEGER);
            put(TypeName.INT.box(), SQLiteHelper.INTEGER);
            put(TypeName.LONG.box(), SQLiteHelper.INTEGER);
            put(TypeName.FLOAT.box(), SQLiteHelper.REAL);
            put(TypeName.DOUBLE.box(), SQLiteHelper.REAL);
            put(TypeName.BOOLEAN.box(), SQLiteHelper.INTEGER);
            put(TypeName.CHAR.box(), SQLiteHelper.TEXT);
            put(ClassName.get(String.class), SQLiteHelper.TEXT);
            put(ArrayTypeName.of(TypeName.BYTE.box()), SQLiteHelper.BLOB);
            put(ArrayTypeName.of(TypeName.BYTE), SQLiteHelper.BLOB);
            put(ClassName.get(Blob.class), SQLiteHelper.BLOB);
        }
    };

    public static SQLiteHelper get(TypeName typeName) {
        SQLiteHelper sqLiteHelper = sTypeMap.get(typeName);

        // fix for enums
        if (sqLiteHelper == null) {
            sqLiteHelper = SQLiteHelper.TEXT;
        }
        return sqLiteHelper;
    }

    public static boolean containsType(TypeName typeName) {
        return sTypeMap.containsKey(typeName);
    }

    private static final Map<TypeName, String> sMethodMap = new HashMap<TypeName, String>() {{
        put(ArrayTypeName.of(TypeName.BYTE), "getBlob");
        put(ArrayTypeName.of(TypeName.BYTE.box()), "getBlob");
        put(TypeName.BOOLEAN, "getInt");
        put(TypeName.BYTE, "getInt");
        put(TypeName.BYTE.box(), "getInt");
        put(TypeName.CHAR, "getString");
        put(TypeName.CHAR.box(), "getString");
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
        put(TypeName.BYTE, "getByt");
        put(TypeName.BYTE.box(), "getByte");
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

    private static Set<TypeName> sNumberMethodList = new HashSet<TypeName>() {{
        add(TypeName.BYTE);
        add(TypeName.DOUBLE);
        add(TypeName.FLOAT);
        add(TypeName.LONG);
        add(TypeName.SHORT);
        add(TypeName.INT);
    }};

    public static boolean containsNumberMethod(TypeName typeName) {
        return sNumberMethodList.contains(typeName);
    }

}
