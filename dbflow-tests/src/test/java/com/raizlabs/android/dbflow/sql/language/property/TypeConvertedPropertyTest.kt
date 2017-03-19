package com.raizlabs.android.dbflow.sql.language.property

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.converter.DateConverter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class TypeConvertedPropertyTest : BaseUnitTest() {


    @Test
    fun testTypeConverter() {
        val property = TypeConvertedProperty<Long, Date>(SimpleModel::class.java, "Prop", true,
            TypeConvertedProperty.TypeConverterGetter { DateConverter() })
        assertEquals("`Prop`", property.toString())

        val date = Date()
        assertEquals("`Prop`=${date.time}", property.eq(date).query)

        val inverted = property.invertProperty()
        assertEquals("`Prop`=5050505", inverted.eq(5050505).query)
    }
}