package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.config.databaseForTable
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table.name
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table.id
import com.dbflow5.query.NameAlias
import com.dbflow5.query.cast
import com.dbflow5.query.createTempTrigger
import com.dbflow5.query.createTrigger
import com.dbflow5.query.insert
import com.dbflow5.query.insertOn
import com.dbflow5.query.property.property
import com.dbflow5.query.select
import com.dbflow5.query.updateOn
import com.dbflow5.sql.SQLiteType
import com.dbflow5.structure.insert
import org.junit.Assert.assertNotNull
import org.junit.Test

class TriggerTest : BaseUnitTest() {

    @Test
    fun validateBasicTrigger() {
        ("CREATE TRIGGER IF NOT EXISTS `MyTrigger` AFTER INSERT ON `SimpleModel` " +
            "\nBEGIN" +
            "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
            "\nEND").assertEquals(createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
            insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name")))
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
                    insert(TwoColumnModel::class, name).values(NameAlias.ofTable("new", "name"))
                    and
                    insert(TwoColumnModel::class, id)
                        .values(cast(NameAlias.ofTable("new", "name").property).`as`(SQLiteType.INTEGER))

            )
    }

    @Test
    fun validateTriggerWorks() {
        databaseForTable<SimpleModel> { db ->
            val trigger = createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
                insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name"))
            trigger.enable(db)
            SimpleModel("Test").insert(db)

            val result = select from TwoColumnModel::class where (name eq "Test")
            assertNotNull(result)
        }
    }
}