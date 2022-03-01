package com.dbflow5.converter

import com.dbflow5.data.Blob

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
 * Reverses conversions.
 */
fun <Data : Any, Model : Any> TypeConverter<Data, Model>.invert() =
    object : TypeConverter<Model, Data>() {
        override fun getDBValue(model: Data): Model {
            return getModelValue(model)
        }

        override fun getModelValue(data: Model): Data {
            return getDBValue(data)
        }
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
 * Description: Converts a boolean object into an Integer for database storage.
 */
@com.dbflow5.annotation.TypeConverter
class BooleanConverter : TypeConverter<Int, Boolean>() {
    override fun getDBValue(model: Boolean): Int = if (model) 1 else 0

    override fun getModelValue(data: Int): Boolean = data == 1
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

@com.dbflow5.annotation.TypeConverter
class BlobConverter : TypeConverter<ByteArray, Blob>() {
    override fun getDBValue(model: Blob) = model.blob

    override fun getModelValue(data: ByteArray) = Blob(data)
}
