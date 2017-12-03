package com.raizlabs.dbflow5.converter

import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Time
import java.sql.Timestamp
import java.util.*

/**
 * Author: andrewgrosner
 * Description: This class is responsible for converting the stored database value into the field value in
 * a Model.
 */
@com.raizlabs.dbflow5.dbflow.annotation.TypeConverter
abstract class TypeConverter<DataClass, ModelClass> {

    /**
     * Converts the ModelClass into a DataClass
     *
     * @param model this will be called upon syncing
     * @return The DataClass value that converts into a SQLite type
     */
    abstract fun getDBValue(model: ModelClass?): DataClass?

    /**
     * Converts a DataClass from the DB into a ModelClass
     *
     * @param data This will be called when the model is loaded from the DB
     * @return The ModelClass value that gets set in a Model that holds the data class.
     */
    abstract fun getModelValue(data: DataClass?): ModelClass?
}

/**
 * Description: Defines how we store and retrieve a [java.math.BigDecimal]
 */
class BigDecimalConverter : TypeConverter<String, BigDecimal>() {
    override fun getDBValue(model: BigDecimal?): String? = model?.toString()

    override fun getModelValue(data: String?): BigDecimal? = if (data == null) null else BigDecimal(data)
}

/**
 * Description: Defines how we store and retrieve a [java.math.BigInteger]
 */
class BigIntegerConverter : TypeConverter<String, BigInteger>() {
    override fun getDBValue(model: BigInteger?): String? = model?.toString()

    override fun getModelValue(data: String?): BigInteger? = if (data == null) null else BigInteger(data)
}

/**
 * Description: Converts a boolean object into an Integer for database storage.
 */
class BooleanConverter : TypeConverter<Int, Boolean>() {
    override fun getDBValue(model: Boolean?): Int? = if (model == null) null else if (model) 1 else 0

    override fun getModelValue(data: Int?): Boolean? = if (data == null) null else data == 1
}

/**
 * Description: Defines how we store and retrieve a [java.util.Calendar]
 */
@com.raizlabs.dbflow5.dbflow.annotation.TypeConverter(allowedSubtypes = arrayOf(GregorianCalendar::class))
class CalendarConverter : TypeConverter<Long, Calendar>() {

    override fun getDBValue(model: Calendar?): Long? = model?.timeInMillis

    override fun getModelValue(data: Long?): Calendar? =
            if (data != null) Calendar.getInstance().apply { timeInMillis = data } else null
}

/**
 * Description: Converts a [Character] into a [String] for database storage.
 */
class CharConverter : TypeConverter<String, Char>() {

    override fun getDBValue(model: Char?): String? =
            if (model != null) String(charArrayOf(model)) else null

    override fun getModelValue(data: String?): Char? = if (data != null) data[0] else null
}

/**
 * Description: Defines how we store and retrieve a [java.util.Date]
 */
class DateConverter : TypeConverter<Long, Date>() {

    override fun getDBValue(model: Date?): Long? = model?.time

    override fun getModelValue(data: Long?): Date? = if (data == null) null else Date(data)
}

/**
 * Description: Defines how we store and retrieve a [java.sql.Date]
 */
@com.raizlabs.dbflow5.dbflow.annotation.TypeConverter(allowedSubtypes = arrayOf(Time::class, Timestamp::class))
class SqlDateConverter : TypeConverter<Long, java.sql.Date>() {

    override fun getDBValue(model: java.sql.Date?): Long? = model?.time

    override fun getModelValue(data: Long?): java.sql.Date? = if (data == null) null else java.sql.Date(data)
}

/**
 * Description: Responsible for converting a [UUID] to a [String].
 *
 * @author Andrew Grosner (fuzz)
 */
class UUIDConverter : TypeConverter<String, UUID>() {

    override fun getDBValue(model: UUID?): String? = model?.toString()

    override fun getModelValue(data: String?): UUID? {
        return if (data == null) {
            null
        } else UUID.fromString(data)
    }
}