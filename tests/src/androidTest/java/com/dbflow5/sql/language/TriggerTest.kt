package com.dbflow5.sql.language

import com.dbflow5.TestDatabase_Database
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.NameAlias
import com.dbflow5.query.createTrigger
import com.dbflow5.query.insert
import com.dbflow5.query.methods.cast
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.twoColumnModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class TriggerTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun validateBasicTrigger() = runBlockingTest {
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
    fun validateUpdateTriggerMultiline() = runBlockingTest {
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
    fun validateTriggerWorks() = dbRule.runBlockingTest {
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