package com.raizlabs.android.dbflow.test.flowmanager;

import com.raizlabs.android.dbflow.converter.TypeConverter;

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
