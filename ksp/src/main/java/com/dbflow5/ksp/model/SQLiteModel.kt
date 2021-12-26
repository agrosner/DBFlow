package com.dbflow5.ksp.model

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

/**
 * Description:
 */
enum class SQLiteModel(
    val sqliteName: String,
    vararg val associatedTypes: TypeName
) {
    Integer(
        "INTEGER",
        BYTE,
        SHORT,
        LONG,
        INT,
    ),
    Real(
        "REAL",
        FLOAT,
        DOUBLE,
    ),
    Blob(
        "BLOB",
        Array::class.asClassName().parameterizedBy(BYTE),
        BYTE_ARRAY,
    ),
    Text(
        "TEXT",
        CHAR,
        STRING,
    )
}

class SQLiteLookup {

    /**
     * Retrieves the [SQLiteModel] for [TypeName]
     */
    fun sqliteName(typeName: TypeName): SQLiteModel = SQLiteModel.values()
        .firstOrNull { it.associatedTypes.contains(typeName) }
        ?: throw IllegalArgumentException("Could not find $typeName in ${SQLiteModel::class}")
}