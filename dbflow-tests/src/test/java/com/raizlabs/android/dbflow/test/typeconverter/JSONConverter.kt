package com.raizlabs.android.dbflow.test.typeconverter

import com.raizlabs.android.dbflow.converter.TypeConverter

import org.json.JSONException
import org.json.JSONObject

/**
 * Description:
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter
class JSONConverter : TypeConverter<String, JSONObject>() {

    override fun getDBValue(model: JSONObject?): String? {
        return model?.toString()
    }

    override fun getModelValue(data: String): JSONObject? {
        var jsonObject: JSONObject? = null
        try {
            jsonObject = JSONObject(data)
        } catch (e: JSONException) {

        } finally {
            return jsonObject
        }
    }
}
