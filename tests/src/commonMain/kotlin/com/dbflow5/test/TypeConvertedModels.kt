package com.dbflow5.test

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.converter.TypeConverter
import com.dbflow5.data.Blob


data class CustomType(val name: Int = 0)

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

class CustomEnumTypeConverter : TypeConverter<String, Difficulty>() {
    override fun getDBValue(model: Difficulty) = model.name.substring(0..0)

    override fun getModelValue(data: String) = when (data) {
        "E" -> Difficulty.EASY
        "M" -> Difficulty.MEDIUM
        "H" -> Difficulty.HARD
        else -> Difficulty.HARD
    }
}

@Table
data class EnumTypeConverterModel(
    @PrimaryKey val id: Int,
    @Column val blob: Blob?,
    @Column val byteArray: ByteArray?,
    @Column(typeConverter = CustomEnumTypeConverter::class)
    val difficulty: Difficulty,
)
