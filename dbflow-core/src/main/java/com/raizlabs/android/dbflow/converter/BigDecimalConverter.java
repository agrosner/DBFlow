package com.raizlabs.android.dbflow.converter;

import java.math.BigDecimal;

/**
 * Description: Defines how we store and retrieve a {@link java.math.BigDecimal}
 */
public class BigDecimalConverter extends TypeConverter<String, BigDecimal> {
    @Override
    public String getDBValue(BigDecimal model) {
        return model == null ? null : model.toString();
    }

    @Override
    public BigDecimal getModelValue(String data) {
        return data == null ? null : new BigDecimal(data);
    }
}
