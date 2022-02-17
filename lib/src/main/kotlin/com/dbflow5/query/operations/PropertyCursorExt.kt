@file:Suppress("unused")

package com.dbflow5.query.operations

import com.dbflow5.annotation.Table
import com.dbflow5.database.FlowCursor

@JvmName("getNullable")
inline fun <Table : Any> Property<String?, Table>.infer(
    cursor: FlowCursor,
    defValue: String? = null
): String? =
    defValue?.let {
        cursor.getStringOrDefault(
            nameAlias.nameRaw(),
            it
        )
    } ?: cursor.getStringOrDefault(nameAlias.nameRaw())

@JvmName("getNullable")
inline fun <Table : Any> Property<String?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: String? = null
): String? =
    defValue?.let {
        cursor.getStringOrDefault(
            index,
            it
        )
    } ?: cursor.getStringOrDefault(index)

inline fun <Table : Any> Property<String, Table>.infer(
    cursor: FlowCursor,
    defValue: String = ""
): String =
    cursor.getStringOrDefault(nameAlias.nameRaw(), defValue)

inline fun <Table : Any> Property<String, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: String = ""
): String =
    cursor.getStringOrDefault(index, defValue)


@JvmName("getNullable")
inline fun <Table : Any> Property<Boolean?, Table>.infer(
    cursor: FlowCursor,
    defValue: Boolean? = null
): Boolean? =
    cursor.getBooleanOrDefault(
        nameAlias.nameRaw(),
        defValue
    )

@JvmName("getNullable")
inline fun <Table : Any> Property<Boolean?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Boolean? = null
): Boolean? =
    cursor.getBooleanOrDefault(
        index,
        defValue
    )

inline fun <Table : Any> Property<Boolean, Table>.infer(
    cursor: FlowCursor,
    defValue: Boolean = false
): Boolean =
    cursor.getBooleanOrDefault(nameAlias.nameRaw(), defValue)

inline fun <Table : Any> Property<Boolean, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Boolean = false
): Boolean =
    cursor.getBooleanOrDefault(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Int?, Table>.infer(
    cursor: FlowCursor,
    defValue: Int? = null
): Int? =
    cursor.getIntOrDefault(nameAlias.nameRaw(), defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Int?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Int? = null
): Int? =
    cursor.getIntOrDefault(index, defValue)

inline fun <Table : Any> Property<Int, Table>.infer(
    cursor: FlowCursor,
    defValue: Int = 0
): Int =
    cursor.getIntOrDefault(nameAlias.nameRaw(), defValue)

inline fun <Table : Any> Property<Int, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Int = 0
): Int =
    cursor.getIntOrDefault(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Double?, Table>.infer(
    cursor: FlowCursor,
    defValue: Double? = null
): Double? =
    cursor.getDoubleOrDefault(nameAlias.nameRaw(), defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Double?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Double? = null
): Double? =
    cursor.getDoubleOrDefault(index, defValue)

inline fun <Table : Any> Property<Double, Table>.infer(
    cursor: FlowCursor,
    defValue: Double = 0.0
): Double =
    cursor.getDoubleOrDefault(nameAlias.nameRaw(), defValue)

inline fun <Table : Any> Property<Double, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Double = 0.0
): Double =
    cursor.getDoubleOrDefault(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Float?, Table>.infer(
    cursor: FlowCursor,
    defValue: Float? = null
): Float? =
    cursor.getFloatOrDefault(nameAlias.nameRaw(), defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Float?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Float? = null
): Float? =
    cursor.getFloatOrDefault(index, defValue)

inline fun <Table : Any> Property<Float, Table>.infer(
    cursor: FlowCursor,
    defValue: Float = 0f
): Float =
    cursor.getFloatOrDefault(nameAlias.nameRaw(), defValue)

inline fun <Table : Any> Property<Float, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Float = 0f
): Float =
    cursor.getFloatOrDefault(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Long?, Table>.infer(
    cursor: FlowCursor,
    defValue: Long? = null
): Long? =
    cursor.getLongOrDefault(nameAlias.nameRaw(), defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Long?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Long? = null
): Long? =
    cursor.getLongOrDefault(index, defValue)

inline fun <Table : Any> Property<Long, Table>.infer(
    cursor: FlowCursor,
    defValue: Long = 0L
): Long =
    cursor.getLongOrDefault(nameAlias.nameRaw(), defValue)

inline fun <Table : Any> Property<Long, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Long = 0L
): Long =
    cursor.getLongOrDefault(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Short?, Table>.infer(
    cursor: FlowCursor,
    defValue: Short? = null
): Short? =
    cursor.getShortOrDefault(nameAlias.nameRaw(), defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Short?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Short? = null
): Short? =
    cursor.getShortOrDefault(index, defValue)

inline fun <Table : Any> Property<Short, Table>.infer(
    cursor: FlowCursor,
    defValue: Short = 0
): Short =
    cursor.getShortOrDefault(nameAlias.nameRaw(), defValue)

inline fun <Table : Any> Property<Short, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Short = 0
): Short =
    cursor.getShortOrDefault(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<ByteArray?, Table>.infer(
    cursor: FlowCursor,
    defValue: ByteArray? = null
): ByteArray? =
    cursor.getBlobOrDefault(nameAlias.nameRaw(), defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<ByteArray?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: ByteArray? = null
): ByteArray? =
    cursor.getBlobOrDefault(index, defValue)

inline fun <Table : Any> Property<ByteArray, Table>.infer(
    cursor: FlowCursor,
    defValue: ByteArray = byteArrayOf()
): ByteArray =
    cursor.getBlobOrDefault(nameAlias.nameRaw(), defValue)

inline fun <Table : Any> Property<ByteArray, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: ByteArray = byteArrayOf()
): ByteArray =
    cursor.getBlobOrDefault(index, defValue)

@Suppress("unused")
inline fun <Data : Any, Model : Any, Table : Any> TypeConvertedProperty<Model, Data, Table>.infer(
    getData: TypeConvertedProperty<Model, Data, Table>.() -> Data
): Model =
    typeConverter<Data, Model>().getModelValue(getData())

@JvmName("inferNullable")
@Suppress("unused")
inline fun <Data : Any, Model : Any, Table : Any> TypeConvertedProperty<Model?, Data, Table>.infer(
    getData: TypeConvertedProperty<Model?, Data, Table>.() -> Data?
): Model? =
    getData()?.let { typeConverter<Data, Model>().getModelValue(it) }

@JvmName("inferNullableData")
@Suppress("unused")
inline fun <Data : Any, Model : Any, Table : Any> TypeConvertedProperty<Model, Data?, Table>.infer(
    getData: TypeConvertedProperty<Model, Data?, Table>.() -> Data
): Model = typeConverter<Data, Model>().getModelValue(getData())

@JvmName("inferNullableDataModel")
@Suppress("unused")
inline fun <Data : Any, Model : Any, Table : Any> TypeConvertedProperty<Model?, Data?, Table>.infer(
    getData: TypeConvertedProperty<Model?, Data?, Table>.() -> Data?
): Model? =
    getData()?.let { typeConverter<Data, Model>().getModelValue(it) }


@JvmName("inferNullable")
inline fun <E : Enum<*>?, Table : Any> Property<E, Table>.infer(
    cursor: FlowCursor,
    enumValueOf: (value: String) -> E
): E? =
    cursor.getStringOrDefault(nameAlias.nameRaw())?.let(enumValueOf)

@JvmName("inferNullable")
inline fun <E : Enum<*>?, Table : Any> Property<E, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    enumValueOf: (value: String) -> E
): E? =
    cursor.getStringOrDefault(index)?.let(enumValueOf)

inline fun <E : Enum<*>, Table : Any> Property<E, Table>.infer(
    cursor: FlowCursor,
    enumValueOf: (value: String) -> E
): E =
    enumValueOf(cursor.getStringOrDefault(nameAlias.nameRaw(), ""))

inline fun <E : Enum<*>, Table : Any> Property<E, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    enumValueOf: (value: String) -> E
): E =
    enumValueOf(cursor.getStringOrDefault(index, ""))
