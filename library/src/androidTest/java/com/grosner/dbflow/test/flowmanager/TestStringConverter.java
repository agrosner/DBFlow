package com.grosner.dbflow.test.flowmanager;

import com.grosner.dbflow.converter.TypeConverter;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
public class TestStringConverter extends TypeConverter<String, String> {

    @Override
    public String getDBValue(String model) {
        return model;
    }

    @Override
    public String getModelValue(String data) {
        return data;
    }
}
