package com.raizlabs.android.dbflow.test.typeconverter;

import com.raizlabs.android.dbflow.converter.TypeConverter;

/**
 * Description: Example of a non-standard typeconverter thats used locally.
 */
public class CustomBooleanConverter extends TypeConverter<String, Boolean> {

    @Override
    public String getDBValue(Boolean model) {
        return model == null ? "" : model ? "1" : "0";
    }

    @Override
    public Boolean getModelValue(String data) {
        return (data == null || data.length() == 0) ? null : Boolean.getBoolean(data);
    }
}
