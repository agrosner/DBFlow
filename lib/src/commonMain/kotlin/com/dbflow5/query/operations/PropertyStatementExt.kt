@file:Suppress("unused")

package com.dbflow5.query.operations

import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.bind
import kotlin.jvm.JvmName

inline fun <ValueType : Any, Table : Any> Property<ValueType?, Table>.bindProperty(
    value: ValueType?,
    statement: DatabaseStatement,
    index: Int,
    statementBinder: (value: ValueType) -> Unit,
) {
    if (value != null) {
        statementBinder(value)
    } else {
        statement.bindNull(index)
    }
}

inline fun <Table : Any> Property<String, Table>.bindProperty(
    value: String,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

@JvmName("bindNullable")
inline fun <Table : Any> Property<String?, Table>.bindProperty(
    value: String?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Table : Any> Property<Boolean, Table>.bindProperty(
    value: Boolean,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun <Table : Any> Property<Boolean?, Table>.bindProperty(
    value: Boolean?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Table : Any> Property<Int, Table>.bindProperty(
    value: Int,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun <Table : Any> Property<Int?, Table>.bindProperty(
    value: Int?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Table : Any> Property<Double, Table>.bindProperty(
    value: Double,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun <Table : Any> Property<Double?, Table>.bindProperty(
    value: Double?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Table : Any> Property<Float, Table>.bindProperty(
    value: Float,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun <Table : Any> Property<Float?, Table>.bindProperty(
    value: Float?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Table : Any> Property<Long, Table>.bindProperty(
    value: Long,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun <Table : Any> Property<Long?, Table>.bindProperty(
    value: Long?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Table : Any> Property<Short, Table>.bindProperty(
    value: Short,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun <Table : Any> Property<Short?, Table>.bindProperty(
    value: Short?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Table : Any> Property<ByteArray, Table>.bindProperty(
    value: ByteArray,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

@JvmName("bindNullable")
inline fun <Table : Any> Property<ByteArray?, Table>.bindProperty(
    value: ByteArray?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Data : Any, Model : Any, Table : Any> TypeConvertedProperty<Model, Data, Table>.bindProperty(
    value: Model,
    statementBinder: (value: Data) -> Unit,
) = statementBinder(typeConverter<Data, Model>().getDBValue(value))

@JvmName("bindNullable")
inline fun <Data : Any, Model : Any, Table : Any> TypeConvertedProperty<Model?, Data, Table>.bindProperty(
    value: Model?,
    statementBinder: (value: Data?) -> Unit,
) = statementBinder(value?.let { typeConverter<Data, Model>().getDBValue(it) })

@JvmName("bindBothNullable")
inline fun <Data : Any, Model : Any, Table : Any> TypeConvertedProperty<Model?, Data?, Table>.bindProperty(
    value: Model?,
    statementBinder: (value: Data?) -> Unit,
) = statementBinder(value?.let { typeConverter<Data, Model>().getDBValue(it) })


inline fun <E : Enum<E>, Table : Any> Property<E, Table>.bindProperty(
    value: E,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

@JvmName("bindNullable")
inline fun <E : Enum<*>, Table : Any> Property<E?, Table>.bindProperty(
    value: E?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}
