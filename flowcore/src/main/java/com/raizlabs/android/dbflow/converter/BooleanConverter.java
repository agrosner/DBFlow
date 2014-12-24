package com.raizlabs.android.dbflow.converter;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class BooleanConverter extends TypeConverter<Integer,Boolean> {
    @Override
    public Integer getDBValue(Boolean model) {
        return model == null ? 0 : model? 1 : 0;
    }

    @Override
    public Boolean getModelValue(Integer data) {
        return data != null && data == 1;
    }
}
