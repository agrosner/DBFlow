@file:Suppress("unused")

package com.dbflow5.query.operations

import com.dbflow5.database.FlowCursor

@JvmName("getNullable")
inline fun <Table : Any> Property<String?, Table>.infer(
    cursor: FlowCursor,
    defValue: String? = null
): String? = cursor.getStringOrNull(
    cursor.getColumnIndex(nameAlias.nameRaw()),
    defValue
)

@JvmName("getNullable")
inline fun <Table : Any> Property<String?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: String? = null
): String? = cursor.getStringOrNull(index, defValue)

inline fun <Table : Any> Property<String, Table>.infer(
    cursor: FlowCursor,
    defValue: String = ""
): String =
    cursor.getString(cursor.getColumnIndex(nameAlias.nameRaw()), defValue)

inline fun <Table : Any> Property<String, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: String = ""
): String = cursor.getString(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Boolean?, Table>.infer(
    cursor: FlowCursor,
    defValue: Boolean? = null
): Boolean? = cursor.getBooleanOrNull(
    cursor.getColumnIndex(nameAlias.nameRaw()),
    defValue
)

@JvmName("getNullable")
inline fun <Table : Any> Property<Boolean?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Boolean? = null
): Boolean? = cursor.getBooleanOrNull(
    index,
    defValue
)

inline fun <Table : Any> Property<Boolean, Table>.infer(
    cursor: FlowCursor,
    defValue: Boolean = false
): Boolean = cursor.getBoolean(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

inline fun <Table : Any> Property<Boolean, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Boolean = false
): Boolean = cursor.getBoolean(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Int?, Table>.infer(
    cursor: FlowCursor,
    defValue: Int? = null
): Int? = cursor.getIntOrNull(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

@JvmName("getNullable")
inline fun <Table : Any> Property<Int?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Int? = null
): Int? = cursor.getIntOrNull(index, defValue)

inline fun <Table : Any> Property<Int, Table>.infer(
    cursor: FlowCursor,
    defValue: Int = 0
): Int = cursor.getInt(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

inline fun <Table : Any> Property<Int, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Int = 0
): Int = cursor.getInt(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Double?, Table>.infer(
    cursor: FlowCursor,
    defValue: Double? = null
): Double? = cursor.getDoubleOrNull(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

@JvmName("getNullable")
inline fun <Table : Any> Property<Double?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Double? = null
): Double? = cursor.getDoubleOrNull(index, defValue)

inline fun <Table : Any> Property<Double, Table>.infer(
    cursor: FlowCursor,
    defValue: Double = 0.0
): Double = cursor.getDouble(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

inline fun <Table : Any> Property<Double, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Double = 0.0
): Double = cursor.getDouble(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Float?, Table>.infer(
    cursor: FlowCursor,
    defValue: Float? = null
): Float? = cursor.getFloatOrNull(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

@JvmName("getNullable")
inline fun <Table : Any> Property<Float?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Float? = null
): Float? = cursor.getFloatOrNull(index, defValue)

inline fun <Table : Any> Property<Float, Table>.infer(
    cursor: FlowCursor,
    defValue: Float = 0f
): Float = cursor.getFloat(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

inline fun <Table : Any> Property<Float, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Float = 0f
): Float = cursor.getFloat(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Long?, Table>.infer(
    cursor: FlowCursor,
    defValue: Long? = null
): Long? = cursor.getLongOrNull(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

@JvmName("getNullable")
inline fun <Table : Any> Property<Long?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Long? = null
): Long? = cursor.getLongOrNull(index, defValue)

inline fun <Table : Any> Property<Long, Table>.infer(
    cursor: FlowCursor,
    defValue: Long = 0L
): Long = cursor.getLong(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

inline fun <Table : Any> Property<Long, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Long = 0L
): Long = cursor.getLong(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<Short?, Table>.infer(
    cursor: FlowCursor,
    defValue: Short? = null
): Short? = cursor.getShortOrNull(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

@JvmName("getNullable")
inline fun <Table : Any> Property<Short?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Short? = null
): Short? = cursor.getShortOrNull(index, defValue)

inline fun <Table : Any> Property<Short, Table>.infer(
    cursor: FlowCursor,
    defValue: Short = 0
): Short = cursor.getShort(cursor.getColumnIndex(nameAlias.nameRaw()), defValue)

inline fun <Table : Any> Property<Short, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: Short = 0
): Short = cursor.getShort(index, defValue)

@JvmName("getNullable")
inline fun <Table : Any> Property<ByteArray?, Table>.infer(
    cursor: FlowCursor,
    defValue: ByteArray? = null
): ByteArray? = cursor.getBlobOrNull(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

@JvmName("getNullable")
inline fun <Table : Any> Property<ByteArray?, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: ByteArray? = null
): ByteArray? = cursor.getBlobOrNull(index, defValue)

inline fun <Table : Any> Property<ByteArray, Table>.infer(
    cursor: FlowCursor,
    defValue: ByteArray = byteArrayOf()
): ByteArray = cursor.getBlob(
    cursor.getColumnIndex(nameAlias.nameRaw()), defValue
)

inline fun <Table : Any> Property<ByteArray, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    defValue: ByteArray = byteArrayOf()
): ByteArray = cursor.getBlob(index, defValue)

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
    cursor.getStringOrNull(cursor.getColumnIndex(nameAlias.nameRaw()))?.let(enumValueOf)

@JvmName("inferNullable")
inline fun <E : Enum<*>?, Table : Any> Property<E, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    enumValueOf: (value: String) -> E
): E? = cursor.getStringOrNull(index)?.let(enumValueOf)

inline fun <E : Enum<*>, Table : Any> Property<E, Table>.infer(
    cursor: FlowCursor,
    enumValueOf: (value: String) -> E
): E = enumValueOf(cursor.getString(cursor.getColumnIndex(nameAlias.nameRaw())))

inline fun <E : Enum<*>, Table : Any> Property<E, Table>.infer(
    cursor: FlowCursor,
    index: Int,
    enumValueOf: (value: String) -> E
): E = enumValueOf(cursor.getString(index))
