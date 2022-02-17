package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.case
import org.junit.Test
import kotlin.test.assertEquals

class CaseTest : BaseUnitTest() {

    @Test
    fun simpleCaseTest() {
        val case = case(CaseModel_Table.country)
            .whenever("USA", then = "Domestic")
            .`else`("Foreign")
            .end("Country")
        assertEquals(
            "CASE `country` " +
                "WHEN 'USA' " +
                "THEN 'Domestic' " +
                "ELSE 'Foreign' END `Country`",
            case.query.trim()
        )
    }

    @Test
    fun searchedCaseTest() {
        val case = case<String?>()
            .whenever(SimpleModel_Table.name.eq("USA"), then = "Domestic")
            .whenever(SimpleModel_Table.name.eq("CA"), then = "Canada")
            .`else`("Foreign")
            .end()
        assertEquals(
            "CASE " +
                "WHEN `name` = 'USA' THEN 'Domestic' " +
                "WHEN `name` = 'CA' THEN 'Canada' " +
                "ELSE 'Foreign' " +
                "END",
            case.query.trim()
        )
    }
}

// TODO: full immutable data class fails in KAPT
@Table
data class CaseModel(
    @PrimaryKey var id: Int = 0,
    var country: String = "",
)