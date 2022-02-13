package com.dbflow5.query2.operations

import com.dbflow5.byteArrayToHexString
import com.dbflow5.converter.TypeConverter
import com.dbflow5.data.Blob
import com.dbflow5.sql.Query
import com.dbflow5.sqlEscapeString

/**
 * Basic interface that describes how a value gets turned into
 * a [String] SQL query.
 */
fun interface SQLValueConverter<ValueType> {
    /**
     * Converts the value to a [String]
     */
    fun convert(value: ValueType): String
}

/**
 * Wraps the [SQLValueConverter] with support for nulls.
 */
fun <ValueType> SQLValueConverter<ValueType>.nullableConverter() =
    SQLValueConverter<ValueType?> { value ->
        if (value == null) UnknownObjectConverter.convert(value)
        else this@nullableConverter.convert(value)
    }

object NoOpValueConverter : SQLValueConverter<Any?> {
    override fun convert(value: Any?): String = value.toString()
}

object NumberQueryConverter : SQLValueConverter<Number> {
    override fun convert(value: Number): String = value.toString()
}

object EnumQueryConverter : SQLValueConverter<Enum<*>> {
    override fun convert(value: Enum<*>): String = sqlEscapeString(value.name)
}

object QueryConverter : SQLValueConverter<Query> {
    override fun convert(value: Query): String = value.query
}

object BlobQueryConverter : SQLValueConverter<Blob> {
    override fun convert(value: Blob): String = ByteArrayQueryConverter.convert(
        value.blob
    )
}

object ByteArrayQueryConverter : SQLValueConverter<ByteArray> {
    override fun convert(value: ByteArray): String =
        "X${sqlEscapeString(byteArrayToHexString(value))}"
}

object UnknownObjectConverter : SQLValueConverter<Any?> {
    override fun convert(value: Any?): String {
        if (value == null) {
            return "NULL"
        }
        return value.toString()
    }
}


/**
 * Constructs a [ModelType] [SQLValueConverter] for [TypeConvertedProperty] types.
 */
fun <ValueType, ModelType> typeConverterValueConverter(
    valueConverter: SQLValueConverter<ValueType>,
    typeConverterGetter: () -> TypeConverter<*, *>,
): TypeConverterValueConverter<ValueType, ModelType> = TypeConverterValueConverter(
    valueConverter,
    typeConverterGetter
)

/**
 * wraps a [SQLValueConverter] with a [typeConverter] to coordinate transformation
 * into a SQL string.
 */
@Suppress("UNCHECKED_CAST")
class TypeConverterValueConverter<ValueType, ModelType>
internal constructor(
    val innerValueConverter: SQLValueConverter<ValueType>,
    typeConverterGetter: () -> TypeConverter<*, *>,
) :
    SQLValueConverter<ModelType> {

    val typeConverter by lazy { typeConverterGetter() as TypeConverter<Any, Any> }
    override fun convert(value: ModelType): String =
        innerValueConverter.convert(typeConverter.getDBValue(value as Any) as ValueType)

}

/**
 * Checks class type of inferred parameter and selects [SQLValueConverter]
 */
inline fun <reified ValueType> inferValueConverter(): SQLValueConverter<ValueType> =
    (when (ValueType::class) {
        Number::class -> NumberQueryConverter
        Enum::class -> EnumQueryConverter
        Query::class -> QueryConverter
        Blob::class -> BlobQueryConverter
        ByteArray::class -> ByteArrayQueryConverter
        else -> UnknownObjectConverter
    } as SQLValueConverter<ValueType>)