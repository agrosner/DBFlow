package com.raizlabs.dbflow5.processor

import com.raizlabs.dbflow5.data.Blob
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName

/**
 * Author: andrewgrosner
 * Description: Holds the mapping between SQL data types and java classes used in the processor.
 */
enum class SQLiteHelper {

    INTEGER {
        override val sqLiteStatementMethod = "Long"

        override val sqliteStatementWrapperMethod: String
            get() = "Number"
    },
    REAL {
        override val sqLiteStatementMethod = "Double"
    },
    TEXT {
        override val sqLiteStatementMethod = "String"
    },
    BLOB {
        override val sqLiteStatementMethod = "Blob"
    };

    abstract val sqLiteStatementMethod: String

    open val sqliteStatementWrapperMethod
        get() = sqLiteStatementMethod

    companion object {

        private val sTypeMap = hashMapOf(TypeName.BYTE to SQLiteHelper.INTEGER,
            TypeName.SHORT to SQLiteHelper.INTEGER,
            TypeName.INT to SQLiteHelper.INTEGER,
            TypeName.LONG to SQLiteHelper.INTEGER,
            TypeName.FLOAT to SQLiteHelper.REAL,
            TypeName.DOUBLE to SQLiteHelper.REAL,
            TypeName.BOOLEAN to SQLiteHelper.INTEGER,
            TypeName.CHAR to SQLiteHelper.TEXT,
            ArrayTypeName.of(TypeName.BYTE) to SQLiteHelper.BLOB,
            TypeName.BYTE.box() to SQLiteHelper.INTEGER,
            TypeName.SHORT.box() to SQLiteHelper.INTEGER,
            TypeName.INT.box() to SQLiteHelper.INTEGER,
            TypeName.LONG.box() to SQLiteHelper.INTEGER,
            TypeName.FLOAT.box() to SQLiteHelper.REAL,
            TypeName.DOUBLE.box() to SQLiteHelper.REAL,
            TypeName.BOOLEAN.box() to SQLiteHelper.INTEGER,
            TypeName.CHAR.box() to SQLiteHelper.TEXT,
            ClassName.get(String::class.java) to SQLiteHelper.TEXT,
            ArrayTypeName.of(TypeName.BYTE.box()) to SQLiteHelper.BLOB,
            ArrayTypeName.of(TypeName.BYTE) to SQLiteHelper.BLOB,
            ClassName.get(Blob::class.java) to SQLiteHelper.BLOB)

        private val sMethodMap = hashMapOf(ArrayTypeName.of(TypeName.BYTE) to "getBlob",
            ArrayTypeName.of(TypeName.BYTE.box()) to "getBlob",
            TypeName.BOOLEAN to "getBoolean",
            TypeName.BYTE to "getInt",
            TypeName.BYTE.box() to "getInt",
            TypeName.CHAR to "getString",
            TypeName.CHAR.box() to "getString",
            TypeName.DOUBLE to "getDouble",
            TypeName.DOUBLE.box() to "getDouble",
            TypeName.FLOAT to "getFloat",
            TypeName.FLOAT.box() to "getFloat",
            TypeName.INT to "getInt",
            TypeName.INT.box() to "getInt",
            TypeName.LONG to "getLong",
            TypeName.LONG.box() to "getLong",
            TypeName.SHORT to "getShort",
            TypeName.SHORT.box() to "getShort",
            ClassName.get(String::class.java) to "getString",
            ClassName.get(Blob::class.java) to "getBlob")

        private val sNumberMethodList = hashSetOf(TypeName.BYTE, TypeName.DOUBLE, TypeName.FLOAT,
            TypeName.LONG, TypeName.SHORT, TypeName.INT)

        operator fun get(typeName: TypeName?): SQLiteHelper = sTypeMap[typeName]
            ?: throw IllegalArgumentException("Cannot map $typeName to a SQLite Type. If this is a " +
            "TypeConverter, ensure it maps to a primitive type.")

        fun getWrapperMethod(typeName: TypeName?): String {
            var sqLiteHelper = get(typeName).sqliteStatementWrapperMethod
            if (typeName == TypeName.FLOAT.box()) {
                sqLiteHelper = "Float"
            }
            return sqLiteHelper
        }

        fun containsType(typeName: TypeName?): Boolean = sTypeMap.containsKey(typeName)

        fun containsMethod(typeName: TypeName?): Boolean = sMethodMap.containsKey(typeName)

        fun getMethod(typeName: TypeName?): String = sMethodMap[typeName] ?: ""

        fun containsNumberMethod(typeName: TypeName?): Boolean = sNumberMethodList.contains(typeName)
    }
}
