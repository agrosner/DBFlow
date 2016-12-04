package com.raizlabs.android.dbflow.test.typeconverter

import com.raizlabs.android.dbflow.converter.TypeConverter

/**
 * Description: Example of a non-standard typeconverter thats used locally.
 */
class CustomBooleanConverter : TypeConverter<String, Boolean>() {

    override fun getDBValue(model: Boolean?): String {
        return if (model == null) "" else if (model) "1" else "0"
    }

    override fun getModelValue(data: String?): Boolean? {
        return if (data == null || data.length == 0) null else java.lang.Boolean.getBoolean(data)
    }
}
