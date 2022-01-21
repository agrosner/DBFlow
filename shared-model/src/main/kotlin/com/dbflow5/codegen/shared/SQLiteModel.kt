package com.dbflow5.codegen.shared

import com.squareup.javapoet.ArrayTypeName
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.javapoet.JTypeName

/**
 * Description:
 */
enum class SQLiteModel(
    val sqliteName: String,
    vararg val associatedTypes: AssociatedType
) {
    Integer(
        "INTEGER",
        AssociatedType(BYTE, JTypeName.BYTE),
        AssociatedType(SHORT, JTypeName.SHORT),
        AssociatedType(LONG, JTypeName.LONG),
        AssociatedType(INT, JTypeName.INT),
    ),
    Real(
        "REAL",
        AssociatedType(FLOAT, JTypeName.FLOAT),
        AssociatedType(DOUBLE, JTypeName.DOUBLE),
    ),
    Blob(
        "BLOB",
        AssociatedType(
            Array::class.asClassName().parameterizedBy(BYTE),
            ArrayTypeName.of(
                JTypeName.BYTE.box(),
            ),
        ),
        AssociatedType(
            BYTE_ARRAY,
            ArrayTypeName.of(
                JTypeName.BYTE,
            ),
        ),
    ),
    Text(
        "TEXT",
        AssociatedType(CHAR, JTypeName.CHAR),
        AssociatedType(STRING, JTypeName.get(String::class.java)),
    )
}

class SQLiteLookup {

    /**
     * Retrieves the [SQLiteModel] for [TypeName]
     */
    fun sqliteName(typeName: TypeName): SQLiteModel {
        return SQLiteModel.values()
            .firstOrNull {
                it.associatedTypes
                    .asSequence()
                    .any { (kTypeName, jTypeName) ->
                        val typeString = typeName.toString()
                        kTypeName.toString() == typeString
                            || jTypeName.toString() == typeString
                            || jTypeName.box().toString() == typeString
                    }
            } ?: throw IllegalArgumentException("Could not find $typeName in ${SQLiteModel::class}")
    }
}