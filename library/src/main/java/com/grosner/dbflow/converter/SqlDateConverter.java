package com.grosner.dbflow.converter;

import java.sql.Date;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines how we store and retrieve a {@link java.sql.Date}
 */
public class SqlDateConverter implements TypeConverter<Long, Date> {

    @Override
    public Long getDBValue(Date model) {
        return model.getTime();
    }

    @Override
    public Date getModelValue(Long data) {
        return new Date(data);
    }
}
