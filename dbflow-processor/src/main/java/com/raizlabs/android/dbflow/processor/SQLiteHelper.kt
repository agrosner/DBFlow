package com.raizlabs.android.dbflow.processor

import com.raizlabs.android.dbflow.data.Blob
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import java.util.*

/**
 * Author: andrewgrosner
 * Description: Holds the mapping between SQL data types and java classes used in the processor.
 */
enum class SQLiteHelper {

    INTEGER {
        override val sqLiteStatementMethod = "Long"
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

    companion object {

        private val sTypeMap = object : HashMap<TypeName, SQLiteHelper>() {
            init {
                put(TypeName.BYTE, SQLiteHelper.INTEGER)
                put(TypeName.SHORT, SQLiteHelper.INTEGER)
                put(TypeName.INT, SQLiteHelper.INTEGER)
                put(TypeName.LONG, SQLiteHelper.INTEGER)
                put(TypeName.FLOAT, SQLiteHelper.REAL)
                put(TypeName.DOUBLE, SQLiteHelper.REAL)
                put(TypeName.BOOLEAN, SQLiteHelper.INTEGER)
                put(TypeName.CHAR, SQLiteHelper.TEXT)
                put(ArrayTypeName.of(TypeName.BYTE), SQLiteHelper.BLOB)
                put(TypeName.BYTE.box(), SQLiteHelper.INTEGER)
                put(TypeName.SHORT.box(), SQLiteHelper.INTEGER)
                put(TypeName.INT.box(), SQLiteHelper.INTEGER)
                put(TypeName.LONG.box(), SQLiteHelper.INTEGER)
                put(TypeName.FLOAT.box(), SQLiteHelper.REAL)
                put(TypeName.DOUBLE.box(), SQLiteHelper.REAL)
                put(TypeName.BOOLEAN.box(), SQLiteHelper.INTEGER)
                put(TypeName.CHAR.box(), SQLiteHelper.TEXT)
                put(ClassName.get(String::class.java), SQLiteHelper.TEXT)
                put(ArrayTypeName.of(TypeName.BYTE.box()), SQLiteHelper.BLOB)
                put(ArrayTypeName.of(TypeName.BYTE), SQLiteHelper.BLOB)
                put(ClassName.get(Blob::class.java), SQLiteHelper.BLOB)
            }
        }

        private val sMethodMap = object : HashMap<TypeName, String>() {
            init {
                put(ArrayTypeName.of(TypeName.BYTE), "getBlob")
                put(ArrayTypeName.of(TypeName.BYTE.box()), "getBlob")
                put(TypeName.BOOLEAN, "getInt")
                put(TypeName.BYTE, "getInt")
                put(TypeName.BYTE.box(), "getInt")
                put(TypeName.CHAR, "getString")
                put(TypeName.CHAR.box(), "getString")
                put(TypeName.DOUBLE, "getDouble")
                put(TypeName.DOUBLE.box(), "getDouble")
                put(TypeName.FLOAT, "getFloat")
                put(TypeName.FLOAT.box(), "getFloat")
                put(TypeName.INT, "getInt")
                put(TypeName.INT.box(), "getInt")
                put(TypeName.LONG, "getLong")
                put(TypeName.LONG.box(), "getLong")
                put(TypeName.SHORT, "getShort")
                put(TypeName.SHORT.box(), "getShort")
                put(ClassName.get(String::class.java), "getString")
                put(ClassName.get(Blob::class.java), "getBlob")
            }
        }

        private val sNumberMethodList = hashSetOf(TypeName.BYTE, TypeName.DOUBLE, TypeName.FLOAT,
                TypeName.LONG, TypeName.SHORT, TypeName.INT)

        operator fun get(typeName: TypeName?): SQLiteHelper = sTypeMap[typeName] ?: SQLiteHelper.TEXT

        fun containsType(typeName: TypeName?): Boolean = sTypeMap.containsKey(typeName)

        fun containsMethod(typeName: TypeName?): Boolean = sMethodMap.containsKey(typeName)

        fun getMethod(typeName: TypeName?): String = sMethodMap[typeName] ?: ""

        fun containsNumberMethod(typeName: TypeName?): Boolean = sNumberMethodList.contains(typeName)
    }
}
