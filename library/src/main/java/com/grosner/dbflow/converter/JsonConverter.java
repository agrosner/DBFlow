package com.grosner.dbflow.converter;

import com.grosner.dbflow.config.FlowLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines how we store and retrieve a {@link org.json.JSONObject}
 */
public class JsonConverter implements TypeConverter<String, JSONObject> {

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
