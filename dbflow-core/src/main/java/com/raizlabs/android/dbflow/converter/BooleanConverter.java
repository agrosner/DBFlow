package com.raizlabs.android.dbflow.converter;

/**
 * Description: Converts a boolean object into an Integer for database storage.
 */
public class BooleanConverter extends TypeConverter<Integer, Boolean> {
    @Override
    public Integer getDBValue(Boolean model) {
        return model == null ? null : model ? 1 : 0;
    }

    @Override
    public Boolean getModelValue(Integer data) {
        return data == null ? null : data == 1;
    }
}
