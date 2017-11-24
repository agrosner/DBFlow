package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.config.writableDatabaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.language.property.eq
import com.raizlabs.android.dbflow.sql.language.property.property
import com.raizlabs.android.dbflow.structure.insert
import org.junit.Assert.assertNotNull
import org.junit.Test

class TriggerTest : BaseUnitTest() {

    @Test
    fun validateBasicTrigger()= writableDatabaseForTable<SimpleModel> {
        assertEquals("CREATE TRIGGER IF NOT EXISTS `MyTrigger` AFTER INSERT ON `SimpleModel` " +
                "\nBEGIN" +
                "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
                "\nEND",
                createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
                        insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name")))
    }

    @Test
    fun validateUpdateTriggerMultiline() = writableDatabaseForTable<SimpleModel> {
        assertEquals("CREATE TEMP TRIGGER IF NOT EXISTS `MyTrigger` BEFORE UPDATE ON `SimpleModel` " +
                "\nBEGIN" +
                "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
                "\nINSERT INTO `TwoColumnModel`(`id`) VALUES(CAST(`new`.`name` AS INTEGER));" +
                "\nEND",
                createTrigger("MyTrigger").temporary().before() updateOn SimpleModel::class begin
                        insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name")) and
                        insert(TwoColumnModel::class)
                                .columnValues(id to cast(NameAlias.ofTable("new", "name").property)
                                        .`as`(SQLiteType.INTEGER)))

    }

    @Test
    fun validateTriggerWorks() = writableDatabaseForTable<SimpleModel>{
        val trigger = createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
                insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name"))
        trigger.enable()
        SimpleModel("Test").insert()

        val result = select from TwoColumnModel::class where (name eq "Test")
        assertNotNull(result)
    }
}