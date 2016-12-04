package com.raizlabs.android.dbflow.test.typeconverter

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.converter.TypeConverter

/**
 * Description: Simple converter to verify that it overrides enum conversion in a [Column].
 */
class EnumOverriderConverter : TypeConverter<Int, EnumOverriderConverter.TestEnum>() {

    enum class TestEnum {
        ONE,
        TWO,
        THREE
    }

    override fun getDBValue(model: TestEnum?): Int? {
        return model?.ordinal
    }

    override fun getModelValue(data: Int?): TestEnum? {
        return if (data == null || data < 0 || data > TestEnum.values().size) null else TestEnum.values()[data]
    }

}
