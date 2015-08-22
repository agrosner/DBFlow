package com.raizlabs.android.dbflow.processor;

import com.raizlabs.android.dbflow.data.Blob;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;

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
        return sTypeMap.get(typeName);
    }

    public static boolean containsType(TypeName typeName) {
        return sTypeMap.containsKey(typeName);
    }


}
