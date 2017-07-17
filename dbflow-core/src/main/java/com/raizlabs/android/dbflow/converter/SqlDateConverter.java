package com.raizlabs.android.dbflow.converter;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Author: andrewgrosner
 * Description: Defines how we store and retrieve a {@link java.sql.Date}
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter(allowedSubtypes = {Time.class, Timestamp.class})
public class SqlDateConverter extends TypeConverter<Long, Date> {

    @Override
    public Long getDBValue(Date model) {
        return model == null ? null : model.getTime();
    }

    @Override
    public Date getModelValue(Long data) {
        return data == null ? null : new Date(data);
    }
}
