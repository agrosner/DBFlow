package com.dbflow5.converter

import com.dbflow5.data.Blob
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Time
import java.sql.Timestamp
import java.util.*

/**
 * Description: This class is responsible for converting the stored database value into the field value in
 * a Model.
 */
abstract class TypeConverter<Data : Any, Model : Any> {

    /**
     * Converts the Model into a Data
     *
     * @param model this will be called upon syncing
     * @return The Data value that converts into a SQLite type
     */
    abstract fun getDBValue(model: Model): Data

    /**
     * Converts a Data from the DB into a Model
     *
     * @param data This will be called when the model is loaded from the DB
     * @return The Model value that gets set in a Model that holds the data class.
     */
    abstract fun getModelValue(data: Data): Model
}

/**
 * Combine two [TypeConverter] into one implementation.
 */
fun <Data : Any, Model1 : Any, Model2 : Any>
    TypeConverter<Data, Model1>.chain(
    typeConverter: TypeConverter<Model1, Model2>
): TypeConverter<Data, Model2> {
    return object : TypeConverter<Data, Model2>() {
        override fun getDBValue(model: Model2): Data {
            return getDBValue(typeConverter.getDBValue(model))
        }

        override fun getModelValue(data: Data): Model2 {
            return typeConverter.getModelValue(this@chain.getModelValue(data))
        }
    }
}

/**
 * Description: Defines how we store and retrieve a [java.math.BigDecimal]
 */
@com.dbflow5.annotation.TypeConverter
class BigDecimalConverter : TypeConverter<String, BigDecimal>() {
    override fun getDBValue(model: BigDecimal): String = model.toString()

    override fun getModelValue(data: String): BigDecimal = BigDecimal(data)
}

/**
 * Description: Defines how we store and retrieve a [java.math.BigInteger]
 */
@com.dbflow5.annotation.TypeConverter
class BigIntegerConverter : TypeConverter<String, BigInteger>() {
    override fun getDBValue(model: BigInteger): String = model.toString()

    override fun getModelValue(data: String): BigInteger = BigInteger(data)
}

/**
 * Description: Converts a boolean object into an Integer for database storage.
 */
@com.dbflow5.annotation.TypeConverter
class BooleanConverter : TypeConverter<Int, Boolean>() {
    override fun getDBValue(model: Boolean): Int = if (model) 1 else 0

    override fun getModelValue(data: Int): Boolean = data == 1
}

/**
 * Description: Defines how we store and retrieve a [java.util.Calendar]
 */
@com.dbflow5.annotation.TypeConverter(allowedSubtypes = [(GregorianCalendar::class)])
class CalendarConverter : TypeConverter<Long, Calendar>() {

    override fun getDBValue(model: Calendar): Long = model.timeInMillis

    override fun getModelValue(data: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = data }
}

/**
 * Description: Converts a [Character] into a [String] for database storage.
 */
@com.dbflow5.annotation.TypeConverter
class CharConverter : TypeConverter<String, Char>() {

    override fun getDBValue(model: Char): String =
        String(charArrayOf(model))

    override fun getModelValue(data: String): Char = data[0]
}

/**
 * Description: Defines how we store and retrieve a [java.util.Date]
 */
@com.dbflow5.annotation.TypeConverter
class DateConverter : TypeConverter<Long, Date>() {

    override fun getDBValue(model: Date): Long = model.time

    override fun getModelValue(data: Long): Date = Date(data)
}

/**
 * Description: Defines how we store and retrieve a [java.sql.Date]
 */
@com.dbflow5.annotation.TypeConverter(allowedSubtypes = [(Time::class), (Timestamp::class)])
class SqlDateConverter : TypeConverter<Long, java.sql.Date>() {

    override fun getDBValue(model: java.sql.Date): Long = model.time

    override fun getModelValue(data: Long): java.sql.Date =
        java.sql.Date(data)
}

/**
 * Description: Responsible for converting a [UUID] to a [String].
 *
 * @author Andrew Grosner (fuzz)
 */
@com.dbflow5.annotation.TypeConverter
class UUIDConverter : TypeConverter<String, UUID>() {

    override fun getDBValue(model: UUID): String = model.toString()

    override fun getModelValue(data: String): UUID = UUID.fromString(data)
}

@com.dbflow5.annotation.TypeConverter
class BlobConverter : TypeConverter<ByteArray, Blob>() {
    override fun getDBValue(model: Blob) = model.blob

    override fun getModelValue(data: ByteArray) = Blob(data)
}
