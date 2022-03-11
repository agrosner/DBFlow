package com.dbflow5.test.sql.language

import com.dbflow5.query.NameAlias
import com.dbflow5.query.createTrigger
import com.dbflow5.query.insert
import com.dbflow5.query.methods.cast
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TwoColumnModel_Table
import com.dbflow5.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class TriggerTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateBasicTrigger() = runTest {
        dbRule {
            ("CREATE TRIGGER IF NOT EXISTS `MyTrigger` AFTER INSERT ON `SimpleModel` " +
                "\nBEGIN" +
                "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
                "\nEND").assertEquals(
                simpleModelAdapter.createTrigger("MyTrigger").after().insertOn() begin
                    twoColumnModelAdapter.insert(
                        TwoColumnModel_Table.name to NameAlias.ofTable(
                            "new",
                            "name"
                        )
                    )
            )
        }
    }

    @Test
    fun validateUpdateTriggerMultiline() = runTest {
        dbRule {
            ("CREATE TEMP TRIGGER IF NOT EXISTS `MyTrigger` BEFORE UPDATE ON `SimpleModel` " +
                "\nBEGIN" +
                "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
                "\nINSERT INTO `TwoColumnModel`(`id`) VALUES(CAST(`new`.`name` AS INTEGER));" +
                "\nEND")
                .assertEquals(
                    simpleModelAdapter.createTrigger(
                        temporary = true,
                        name = "MyTrigger"
                    ).before().updateOn()
                        begin
                        twoColumnModelAdapter.insert(
                            TwoColumnModel_Table.name
                        ).values(NameAlias.ofTable("new", "name"))
                        and
                        twoColumnModelAdapter.insert(TwoColumnModel_Table.id)
                            .values(
                                cast(TwoColumnModel_Table.name.withTable("new")).asInteger()
                            )

                )
        }
    }

    @Test
    fun validateTriggerWorks() = dbRule.runTest {
        val trigger = simpleModelAdapter.createTrigger("MyTrigger").after().insertOn() begin
            twoColumnModelAdapter.insert(
                TwoColumnModel_Table.name to NameAlias.ofTable(
                    "new",
                    "name"
                ),
                TwoColumnModel_Table.id to 1,
            )
        trigger.execute()
        simpleModelAdapter.insert(SimpleModel("Test"))

        val result =
            select from twoColumnModelAdapter where (TwoColumnModel_Table.name eq "Test")
        assertNotNull(result)
    }
}