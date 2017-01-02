package com.raizlabs.android.dbflow.converter;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Author: andrewgrosner
 * Description: Defines how we store and retrieve a {@link java.util.Calendar}
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter(allowedSubtypes = {GregorianCalendar.class})
public class CalendarConverter extends TypeConverter<Long, Calendar> {

    @Override
    public Long getDBValue(Calendar model) {
        return model == null ? null : model.getTimeInMillis();
    }

    @Override
    public Calendar getModelValue(Long data) {
        if (data != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data);
            return calendar;
        } else {
            return null;
        }
    }
}
