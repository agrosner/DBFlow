package com.raizlabs.android.dbflow.converter;

import java.math.BigInteger;

/**
 * Description: Defines how we store and retrieve a {@link java.math.BigInteger}
 */
public class BigIntegerConverter extends TypeConverter<String, BigInteger> {
    @Override
    public String getDBValue(BigInteger model) {
        return model == null ? null : model.toString();
    }

    @Override
    public BigInteger getModelValue(String data) {
        return data == null ? null : new BigInteger(data);
    }
}
