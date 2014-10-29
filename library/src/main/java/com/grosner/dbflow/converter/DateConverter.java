package com.grosner.dbflow.converter;

import java.util.Date;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines how we store and retrieve a {@link java.util.Date}
 */
public class DateConverter implements TypeConverter<Long, Date> {

    @Override
    public Long getDBValue(Date model) {
        return model.getTime();
    }

    @Override
    public Date getModelValue(Long data) {
        return new Date(data);
    }
}
