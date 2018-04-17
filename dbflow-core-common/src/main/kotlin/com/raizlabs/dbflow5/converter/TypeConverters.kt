package com.raizlabs.dbflow5.converter

/**
 * Author: andrewgrosner
 * Description: This class is responsible for converting the stored database value into the field value in
 * a Model.
 */
@com.raizlabs.dbflow5.annotation.TypeConverter
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
 * Description: Converts a boolean object into an Integer for database storage.
 */
class BooleanConverter : TypeConverter<Int, Boolean>() {
    override fun getDBValue(model: Boolean?): Int? = if (model == null) null else if (model) 1 else 0

    override fun getModelValue(data: Int?): Boolean? = if (data == null) null else data == 1
}


/**
 * Description: Converts a [Character] into a [String] for database storage.
 */
class CharConverter : TypeConverter<String, Char>() {

    override fun getDBValue(model: Char?): String? = model?.toString()

    override fun getModelValue(data: String?): Char? = if (data != null) data[0] else null
}

