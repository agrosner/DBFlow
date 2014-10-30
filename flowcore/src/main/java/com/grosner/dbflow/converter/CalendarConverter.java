package com.grosner.dbflow.converter;

import java.util.Calendar;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines how we store and retrieve a {@link java.util.Calendar}
 */
public class CalendarConverter extends TypeConverter<Long, Calendar> {

    @Override
    public Long getDBValue(Calendar model) {
        return model.getTimeInMillis();
    }

    @Override
    public Calendar getModelValue(Long data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(data);
        return calendar;
    }
}
