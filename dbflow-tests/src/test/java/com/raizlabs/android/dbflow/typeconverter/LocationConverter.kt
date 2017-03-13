package com.raizlabs.android.dbflow.typeconverter

import android.location.Location

import com.raizlabs.android.dbflow.converter.TypeConverter

/**
 * Description:
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter
class LocationConverter : TypeConverter<String, Location>() {

    override fun getDBValue(model: Location?): String? {
        return if (model == null) null else String.format("%1s,%1s", model.latitude, model.longitude)
    }

    override fun getModelValue(data: String?): Location? {
        if (data == null) {
            return null
        }
        val values = data.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        val location = Location("")
        location.latitude = java.lang.Double.valueOf(values[0])!!
        location.longitude = java.lang.Double.valueOf(values[1])!!
        return location
    }
}
