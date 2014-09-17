package com.grosner.dbflow.converter;

import com.grosner.dbflow.config.FlowLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class JsonConverter implements TypeConverter<String,JSONObject> {
    @Override
    public Class<String> getDatabaseType() {
        return String.class;
    }

    @Override
    public Class<JSONObject> getModelType() {
        return JSONObject.class;
    }

    @Override
    public String getDBValue(JSONObject model) {
        return model.toString();
    }

    @Override
    public JSONObject getModelValue(String data) {
        try {
            return new JSONObject(data);
        } catch (JSONException e) {
            FlowLog.logError(e);
            return null;
        }
    }
}
