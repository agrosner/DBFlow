package com.grosner.dbflow.test.typeconverter;

import com.grosner.dbflow.converter.TypeConverter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@com.grosner.dbflow.annotation.TypeConverter
public class JSONConverter extends TypeConverter<String, JSONObject> {
    @Override
    public String getDBValue(JSONObject model) {
        return model.toString();
    }

    @Override
    public JSONObject getModelValue(String data) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
        } catch (JSONException e) {

        } finally {
            return jsonObject;
        }
    }
}
