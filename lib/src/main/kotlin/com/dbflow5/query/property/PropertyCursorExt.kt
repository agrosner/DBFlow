package com.dbflow5.query.property

import com.dbflow5.database.FlowCursor


@JvmName("getNullable")
fun Property<String?>.infer(cursor: FlowCursor): String? =
    cursor.getStringOrDefault(nameAlias.nameRaw())

fun Property<String>.infer(cursor: FlowCursor): String =
    cursor.getStringOrDefault(nameAlias.nameRaw(), "")

@JvmName("getNullable")
fun Property<Boolean?>.infer(cursor: FlowCursor): Boolean =
    cursor.getBooleanOrDefault(nameAlias.nameRaw())

fun Property<Boolean>.infer(cursor: FlowCursor): Boolean =
    cursor.getBooleanOrDefault(nameAlias.nameRaw())

@JvmName("getNullable")
fun Property<Int?>.infer(cursor: FlowCursor): Int? =
    cursor.getIntOrDefault(nameAlias.nameRaw(), null)

fun Property<Int>.infer(cursor: FlowCursor): Int =
    cursor.getIntOrDefault(nameAlias.nameRaw())

@JvmName("getNullable")
fun Property<Double?>.infer(cursor: FlowCursor): Double? =
    cursor.getDoubleOrDefault(nameAlias.nameRaw(), null)

fun Property<Double>.infer(cursor: FlowCursor): Double =
    cursor.getDoubleOrDefault(nameAlias.nameRaw())

@JvmName("getNullable")
fun Property<Float?>.infer(cursor: FlowCursor): Float? =
    cursor.getFloatOrDefault(nameAlias.nameRaw(), null)

fun Property<Float>.infer(cursor: FlowCursor): Float =
    cursor.getFloatOrDefault(nameAlias.nameRaw())

@JvmName("getNullable")
fun Property<Long?>.infer(cursor: FlowCursor): Long? =
    cursor.getLongOrDefault(nameAlias.nameRaw(), null)

fun Property<Long>.infer(cursor: FlowCursor): Long =
    cursor.getLongOrDefault(nameAlias.nameRaw())

@JvmName("getNullable")
fun Property<Short?>.infer(cursor: FlowCursor): Short? =
    cursor.getShortOrDefault(nameAlias.nameRaw(), null)

fun Property<Short>.infer(cursor: FlowCursor): Short =
    cursor.getShortOrDefault(nameAlias.nameRaw())

@JvmName("getNullable")
fun Property<ByteArray?>.infer(cursor: FlowCursor): ByteArray? =
    cursor.getBlobOrDefault(nameAlias.nameRaw())

fun Property<ByteArray>.infer(cursor: FlowCursor): ByteArray =
    cursor.getBlobOrDefault(nameAlias.nameRaw(), byteArrayOf())

@Suppress("unused")
inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data, Model>.infer(
    getData: TypeConvertedProperty<Data, Model>.() -> Data
): Model =
    typeConverter<Data, Model>().getModelValue(getData())

@JvmName("inferNullable")
@Suppress("unused")
inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data, Model?>.infer(
    getData: TypeConvertedProperty<Data, Model?>.() -> Data?
): Model? =
    getData()?.let { typeConverter<Data, Model>().getModelValue(it) }

@JvmName("inferNullableData")
@Suppress("unused")
inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data?, Model>.infer(
    getData: TypeConvertedProperty<Data?, Model>.() -> Data
): Model = typeConverter<Data, Model>().getModelValue(getData())

@JvmName("inferNullableDataModel")
@Suppress("unused")
inline fun <Data : Any, Model : Any> TypeConvertedProperty<Data?, Model?>.infer(
    getData: TypeConvertedProperty<Data?, Model?>.() -> Data?
): Model? =
    getData()?.let { typeConverter<Data, Model>().getModelValue(it) }

@JvmName("inferNullable")
fun <E : Enum<*>?> Property<E>.infer(
    cursor: FlowCursor,
    enumValueOf: (value: String) -> E
): E? =
    cursor.getStringOrDefault(nameAlias.nameRaw())?.let(enumValueOf)

fun <E : Enum<*>> Property<E>.infer(
    cursor: FlowCursor,
    enumValueOf: (value: String) -> E
): E =
    enumValueOf(cursor.getStringOrDefault(nameAlias.nameRaw(), ""))