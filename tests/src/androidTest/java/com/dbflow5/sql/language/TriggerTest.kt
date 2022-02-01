package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.NameAlias
import com.dbflow5.query.cast
import com.dbflow5.query.createTempTrigger
import com.dbflow5.query.createTrigger
import com.dbflow5.query.insert
import com.dbflow5.query.property.property
import com.dbflow5.query.select
import com.dbflow5.query.updateOn
import com.dbflow5.simpleModel
import com.dbflow5.sql.SQLiteType
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertNotNull
import org.junit.Test

class TriggerTest : BaseUnitTest() {

    @Test
    fun validateBasicTrigger() {
        ("CREATE TRIGGER IF NOT EXISTS `MyTrigger` AFTER INSERT ON `SimpleModel` " +
            "\nBEGIN" +
            "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
            "\nEND").assertEquals(
            createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
                insert(TwoColumnModel::class).columnValues(
                    TwoColumnModel_Table.name to NameAlias.ofTable(
                        "new",
                        "name"
                    )
                )
        )
    }

    @Test
    fun validateUpdateTriggerMultiline() {
        ("CREATE TEMP TRIGGER IF NOT EXISTS `MyTrigger` BEFORE UPDATE ON `SimpleModel` " +
            "\nBEGIN" +
            "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
            "\nINSERT INTO `TwoColumnModel`(`id`) VALUES(CAST(`new`.`name` AS INTEGER));" +
            "\nEND")
            .assertEquals(
                createTempTrigger("MyTrigger").before()
                    updateOn SimpleModel::class
                    begin
                    insert(
                        TwoColumnModel::class,
                        TwoColumnModel_Table.name
                    ).values(NameAlias.ofTable("new", "name"))
                    and
                    insert(TwoColumnModel::class, TwoColumnModel_Table.id)
                        .values(
                            cast(
                                NameAlias.ofTable(
                                    "new",
                                    "name"
                                ).property
                            ).`as`(SQLiteType.INTEGER)
                        )

            )
    }

    @Test
    fun validateTriggerWorks() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val trigger = createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
                insert(TwoColumnModel::class).columnValues(
                    TwoColumnModel_Table.name to NameAlias.ofTable(
                        "new",
                        "name"
                    ),
                    TwoColumnModel_Table.id to 1,
                )
            trigger.enable(db)
            simpleModel.insert(SimpleModel("Test"))

            val result =
                select from TwoColumnModel::class where (TwoColumnModel_Table.name eq "Test")
            assertNotNull(result)
        }
    }
}