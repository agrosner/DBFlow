package com.raizlabs.android.dbflow.test.typeconverter;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.converter.TypeConverter;

/**
 * Description: Simple converter to verify that it overrides enum conversion in a {@link Column}.
 */
public class EnumOverriderConverter extends TypeConverter<Integer, EnumOverriderConverter.TestEnum> {

    public enum TestEnum {
        ONE,
        TWO,
        THREE
    }

    @Override
    public Integer getDBValue(TestEnum model) {
        return model == null ? null : model.ordinal();
    }

    @Override
    public TestEnum getModelValue(Integer data) {
        return (data == null || data < 0 || data > TestEnum.values().length) ? null : TestEnum.values()[data];
    }

}
