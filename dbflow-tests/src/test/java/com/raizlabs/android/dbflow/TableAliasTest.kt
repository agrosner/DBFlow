package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
import org.junit.Test
import com.raizlabs.android.dbflow.AliasedTable_Table.columnOne
import com.raizlabs.android.dbflow.AliasedTable_Table.columnTwo
import com.raizlabs.android.dbflow.AnotherAliasedTable_Table.anotherColumnOne
import com.raizlabs.android.dbflow.kotlinextensions.innerJoin
import com.raizlabs.android.dbflow.sql.language.NameAlias
import com.raizlabs.android.dbflow.sql.language.SQLite.select

class TableAliasTest : BaseUnitTest() {

    @Test
    fun testQueryWithTableAlias() {
        assertEquals(
            "SELECT `at`.`columnOne`,`at`.`columnTwo` FROM `AliasedTable` AS `at`",
            select(columnOne, columnTwo) from AliasedTable::class
        )
    }

    @Test
    fun testQueryWithTableAliasAndWithTableCall() {
        // show you can override and use withTable
        assertEquals(
            "SELECT `AliasedTable`.`columnOne`,`AliasedTable`.`columnTwo` FROM `AliasedTable`",
            select(columnOne.withTable(), columnTwo.withTable()).from(AliasedTable::class).`as`("")
        )
    }

    @Test
    fun testQueryWithTableAliasAndWithTableColumnAliasCall() {
        // show you can override with custom alias when you want
        val alias: NameAlias = NameAlias.of("at2")
        assertEquals(
            "SELECT `at2`.`columnOne`,`at2`.`columnTwo` FROM `AliasedTable` AS `at2`",
            select(columnOne.withTable(alias), columnTwo.withTable(alias)).from(AliasedTable::class).`as`("at2")
        )
    }

    @Test
    fun testJoinWithTableAlias() {

        assertEquals (
            "SELECT `at`.`columnOne`,`aat`.`anotherColumnOne` FROM `AliasedTable` AS `at` INNER JOIN `AnotherAliasedTable` AS `aat` ON `at`.`anotherId`=`aat`.`id`",
            select(columnOne, anotherColumnOne)
                from AliasedTable::class
                innerJoin AnotherAliasedTable::class
                on AliasedTable_Table.anotherId.eq(AnotherAliasedTable_Table.id)
        )
    }
}

@Table(database = TestDatabase::class, tableAlias = "at")
class AliasedTable(
    @PrimaryKey(autoincrement = true) var id: Int = 0,
    @Column() var columnOne: String = "",
    @Column() var columnTwo: String = "",
    @Column() var anotherId: Int = 0
)

@Table(database = TestDatabase::class, tableAlias = "aat")
class AnotherAliasedTable(
    @PrimaryKey(autoincrement = true) var id: Int = 0,
    @Column() var anotherColumnOne: String = ""
)