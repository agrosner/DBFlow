package com.grosner.dbflow.test.flowmanager;

import com.grosner.dbflow.converter.TypeConverter;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
public class StringConverter implements TypeConverter<String, String> {

    @Override
    public Class<String> getDatabaseType() {
        return String.class;
    }

    @Override
    public Class<String> getModelType() {
        return String.class;
    }

    @Override
    public String getDBValue(String model) {
        return model;
    }

    @Override
    public String getModelValue(String data) {
        return data;
    }
}
