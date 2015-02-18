package com.raizlabs.android.dbflow.converter;

import java.util.Date;

/**
 * Author: andrewgrosner
 * Description: Defines how we store and retrieve a {@link java.util.Date}
 */
public class DateConverter extends TypeConverter<Long, Date> {

    @Override
    public Long getDBValue(Date model) {
        return model == null ? null : model.getTime();
    }

    @Override
    public Date getModelValue(Long data) {
        return data == null ? null : new Date(data);
    }
}
