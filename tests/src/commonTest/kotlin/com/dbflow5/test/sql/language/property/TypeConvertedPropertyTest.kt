package com.dbflow5.test.sql.language.property

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.CustomType
import com.dbflow5.test.Difficulty
import com.dbflow5.test.EnumTypeConverterModel_Table
import com.dbflow5.test.TypeConverterModel_Table
import com.dbflow5.test.DatabaseTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeConvertedPropertyTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testTypeConverter() {
        val property = TypeConverterModel_Table.customType
        assertEquals("`customType`", property.query)

        val value = CustomType(0)
        assertEquals("`customType` = 0", property.eq(value).query)

        assertEquals(
            "`TypeConverterModel`.`customType` = 0",
            property.withTable().eq(value).query
        )

        val inverted = property.dataProperty
        assertEquals("`customType` = 5050505", inverted.eq(5050505).query)
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