@file:Suppress("unused")

package com.dbflow5.query.property

import com.dbflow5.converter.TypeConverter
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.bind

inline fun <T : Any> Property<T?>.bindProperty(
    value: T?,
    statement: DatabaseStatement,
    index: Int,
    statementBinder: (value: T) -> Unit,
) {
    if (value != null) {
        statementBinder(value)
    } else {
        statement.bindNull(index)
    }
}

inline fun Property<String>.bindProperty(
    value: String,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

@JvmName("bindNullable")
inline fun Property<String?>.bindProperty(
    value: String?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun Property<Boolean>.bindProperty(
    value: Boolean,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun Property<Boolean?>.bindProperty(
    value: Boolean?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun Property<Int>.bindProperty(
    value: Int,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun Property<Int?>.bindProperty(
    value: Int?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun Property<Double>.bindProperty(
    value: Double,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun Property<Double?>.bindProperty(
    value: Double?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun Property<Float>.bindProperty(
    value: Float,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun Property<Float?>.bindProperty(
    value: Float?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun Property<Long>.bindProperty(
    value: Long,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun Property<Long?>.bindProperty(
    value: Long?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun Property<Short>.bindProperty(
    value: Short,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

inline fun Property<Short?>.bindProperty(
    value: Short?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun Property<ByteArray>.bindProperty(
    value: ByteArray,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

@JvmName("bindNullable")
inline fun Property<ByteArray?>.bindProperty(
    value: ByteArray?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}

inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data, Model>.bindProperty(
    value: Model,
    statementBinder: (value: Data) -> Unit,
) = statementBinder(typeConverter<Data, Model>().getDBValue(value))

@JvmName("bindNullable")
inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data, Model?>.bindProperty(
    value: Model?,
    statementBinder: (value: Data?) -> Unit,
) = statementBinder(value?.let { typeConverter<Data, Model>().getDBValue(it) })

@JvmName("bindBothNullable")
inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data?, Model?>.bindProperty(
    value: Model?,
    statementBinder: (value: Data?) -> Unit,
) = statementBinder(value?.let { typeConverter<Data, Model>().getDBValue(it) })

inline fun <E : Enum<E>> Property<E>.bindProperty(
    value: E,
    statement: DatabaseStatement,
    index: Int
) = statement.bind(index, value)

@JvmName("bindNullable")
inline fun <E : Enum<*>> Property<E?>.bindProperty(
    value: E?,
    statement: DatabaseStatement,
    index: Int
) = bindProperty(value, statement, index) {
    statement.bind(index, it)
}
