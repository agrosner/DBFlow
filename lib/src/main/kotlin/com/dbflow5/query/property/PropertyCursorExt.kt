package com.dbflow5.query.property

import com.dbflow5.converter.TypeConverter
import com.dbflow5.database.FlowCursor


@JvmName("getNullable")
fun Property<String?>.infer(cursor: FlowCursor): String? =
    cursor.getStringOrDefault(nameAlias.name())

fun Property<String>.infer(cursor: FlowCursor): String =
    cursor.getStringOrDefault(nameAlias.name(), "")

@JvmName("getNullable")
fun Property<Boolean?>.infer(cursor: FlowCursor): Boolean =
    cursor.getBooleanOrDefault(nameAlias.name())

fun Property<Boolean>.infer(cursor: FlowCursor): Boolean =
    cursor.getBooleanOrDefault(nameAlias.name())

@JvmName("getNullable")
fun Property<Int?>.infer(cursor: FlowCursor): Int? =
    cursor.getIntOrDefault(nameAlias.name(), null)

fun Property<Int>.infer(cursor: FlowCursor): Int =
    cursor.getIntOrDefault(nameAlias.name())

@JvmName("getNullable")
fun Property<Double?>.infer(cursor: FlowCursor): Double? =
    cursor.getDoubleOrDefault(nameAlias.name(), null)

fun Property<Double>.infer(cursor: FlowCursor): Double =
    cursor.getDoubleOrDefault(nameAlias.name())

@JvmName("getNullable")
fun Property<Float?>.infer(cursor: FlowCursor): Float? =
    cursor.getFloatOrDefault(nameAlias.name(), null)

fun Property<Float>.infer(cursor: FlowCursor): Float =
    cursor.getFloatOrDefault(nameAlias.name())

@JvmName("getNullable")
fun Property<Long?>.infer(cursor: FlowCursor): Long? =
    cursor.getLongOrDefault(nameAlias.name(), null)

fun Property<Long>.infer(cursor: FlowCursor): Long =
    cursor.getLongOrDefault(nameAlias.name())

@JvmName("getNullable")
fun Property<Short?>.infer(cursor: FlowCursor): Short? =
    cursor.getShortOrDefault(nameAlias.name(), null)

fun Property<Short>.infer(cursor: FlowCursor): Short =
    cursor.getShortOrDefault(nameAlias.name())

@JvmName("getNullable")
fun Property<ByteArray?>.infer(cursor: FlowCursor): ByteArray? =
    cursor.getBlobOrDefault(nameAlias.name())

fun Property<ByteArray>.infer(cursor: FlowCursor): ByteArray =
    cursor.getBlobOrDefault(nameAlias.name(), byteArrayOf())

@Suppress("unused")
inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data, Model>.infer(
    cursor: FlowCursor, typeConverter: TypeConverter<Data, Model>,
    getData: (cursor: FlowCursor) -> Data
): Model =
    typeConverter.getModelValue(getData(cursor))

@JvmName("inferNullable")
@Suppress("unused")
inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data, Model?>.infer(
    cursor: FlowCursor, typeConverter: TypeConverter<Data, Model>,
    getData: (cursor: FlowCursor) -> Data?
): Model? =
    getData(cursor)?.let { typeConverter.getModelValue(it) }

@JvmName("inferNullable")
fun <E : Enum<*>?> Property<E>.infer(
    cursor: FlowCursor,
    enumValueOf: (value: String) -> E
): E? =
    cursor.getStringOrDefault(nameAlias.name())?.let(enumValueOf)

fun <E : Enum<*>> Property<E>.infer(
    cursor: FlowCursor,
    enumValueOf: (value: String) -> E
): E =
    enumValueOf(cursor.getStringOrDefault(nameAlias.name(), ""))