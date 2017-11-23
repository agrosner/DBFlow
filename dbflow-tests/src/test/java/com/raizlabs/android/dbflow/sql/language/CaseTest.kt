package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import com.raizlabs.android.dbflow.sql.language.property.propertyString
import org.junit.Assert.*
import org.junit.Test

class CaseTest : BaseUnitTest() {

    @Test
    fun simpleCaseTest() {
        val case = case<String>(propertyString<String>("country"))
                .whenever("USA")
                .then("Domestic")
                .`else`("Foreign")
        assertEquals("CASE country WHEN 'USA' THEN 'Domestic' ELSE 'Foreign' END `Country`",
                case.end("Country").query.trim())
        assertTrue(case.isEfficientCase)
    }

    @Test
    fun searchedCaseTest() {
        val case = caseWhen<String>(SimpleModel_Table.name.eq("USA")).then("Domestic")
                .whenever(SimpleModel_Table.name.eq("CA")).then("Canada")
                .`else`("Foreign")
        assertEquals("CASE WHEN `name`='USA' THEN 'Domestic' WHEN `name`='CA' THEN 'Canada' ELSE 'Foreign'",
                case.query.trim())
        assertFalse(case.isEfficientCase)
    }
}