package com.raizlabs.android.dbflow.test.typeconverter;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description:
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter
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
