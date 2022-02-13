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
        if (value == null) InferredObjectConverter.convert(value)
        else this@nullableConverter.convert(value)
    }

object NumberQueryConverter : SQLValueConverter<Number> {
    override fun convert(value: Number): String = value.toString()
}

object EnumQueryConverter : SQLValueConverter<Enum<*>> {
    override fun convert(value: Enum<*>): String = sqlEscapeString(value.name)
}

object QueryConverter : SQLValueConverter<Query> {
    override fun convert(value: Query): String = value.query.trim()
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

/**
 * Encases values with [sqlEscapeString]
 */
object StringQueryConverter : SQLValueConverter<String> {
    override fun convert(value: String): String =
        sqlEscapeString(value)
}

/**
 * Converts values literally:
 * 1. null still is NULL
 * 2. Strings are NOT escaped
 * 3. Enum Names are NOT escaped
 * 4. [Query] value still calls the [QueryConverter]
 * 5. Anything else is toString()
 *
 */
object LiteralValueConverter : SQLValueConverter<Any?> {
    override fun convert(value: Any?): String {
        return when (value) {
            null -> "NULL"
            is Query -> QueryConverter.convert(value)
            is Enum<*> -> value.name
            else -> value.toString()
        }
    }
}

/**
 * Checks value instance type and checks.
 */
object InferredObjectConverter : SQLValueConverter<Any?> {
    override fun convert(value: Any?): String {
        return when (value) {
            null -> "NULL"
            is Query -> QueryConverter.convert(value)
            is Number -> NumberQueryConverter.convert(value)
            is Enum<*> -> EnumQueryConverter.convert(value)
            is Blob -> BlobQueryConverter.convert(value)
            is ByteArray -> ByteArrayQueryConverter.convert(value)
            is String -> StringQueryConverter.convert(value)
            else -> sqlEscapeString(value.toString())
        }
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
@Suppress("UNCHECKED_CAST")
inline fun <reified ValueType> inferValueConverter(): SQLValueConverter<ValueType> =
// if compiler can find a pure match here, use them directly, otherwise
    // fallback on the inferred value checker.
    (when (ValueType::class) {
        Number::class,
        Double::class,
        Int::class,
        Float::class,
        Long::class,
        Short::class,
        Byte::class,
        -> NumberQueryConverter
        Enum::class -> EnumQueryConverter
        Query::class -> QueryConverter
        Blob::class -> BlobQueryConverter
        ByteArray::class -> ByteArrayQueryConverter
        String::class -> StringQueryConverter
        else -> InferredObjectConverter
    } as SQLValueConverter<ValueType>)