package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.converter.DateConverter
import com.dbflow5.models.Difficulty
import com.dbflow5.models.EnumTypeConverterModel_Table
import com.dbflow5.query.property.TypeConvertedProperty
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class TypeConvertedPropertyTest : BaseUnitTest() {

    private val simpleModelAdapter
        get() = database<TestDatabase>().simpleModelAdapter

    @Test
    fun testTypeConverter() {
        val property = TypeConvertedProperty<Long, Date>(
            simpleModelAdapter, "Prop", true
        ) { DateConverter() }
        assertEquals("`Prop`", property.toString())

        val date = Date()
        assertEquals("`Prop`=${date.time}", property.eq(date).query)

        assertEquals("`SimpleModel`.`Prop`=${date.time}", property.withTable().eq(date).query)

        val inverted = property.invertProperty()
        assertEquals("`Prop`=5050505", inverted.eq(5050505).query)
    }

    @Test
    fun testCustomEnumTypeConverter() {

        assertEquals(
            "`difficulty` = 'H'",
            EnumTypeConverterModel_Table.difficulty.eq(Difficulty.HARD).query
        )
        assertEquals(
            "`EnumTypeConverterModel`.`difficulty` = 'H'",
            EnumTypeConverterModel_Table.difficulty.withTable().eq(Difficulty.HARD).query
        )
        assertEquals(
            "`et`.`difficulty` = 'H'",
            EnumTypeConverterModel_Table.difficulty.withTable(
                "et"
            ).eq(Difficulty.HARD).query
        )
    }
}