package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.kotlinextensions.and
import com.raizlabs.android.dbflow.kotlinextensions.begin
import com.raizlabs.android.dbflow.kotlinextensions.columnValues
import com.raizlabs.android.dbflow.kotlinextensions.createTrigger
import com.raizlabs.android.dbflow.kotlinextensions.eq
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.insert
import com.raizlabs.android.dbflow.kotlinextensions.insertOn
import com.raizlabs.android.dbflow.kotlinextensions.property
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.updateOn
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.sql.SQLiteType
import org.junit.Assert.assertNotNull
import org.junit.Test

class TriggerTest : BaseUnitTest() {

    @Test
    fun validateBasicTrigger() {
        assertEquals("CREATE TRIGGER IF NOT EXISTS `MyTrigger` AFTER INSERT ON `SimpleModel` " +
            "\nBEGIN" +
            "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
            "\nEND",
            createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
                insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name")))
    }

    @Test
    fun validateUpdateTriggerMultiline() {
        assertEquals("CREATE TEMP TRIGGER IF NOT EXISTS `MyTrigger` BEFORE UPDATE ON `SimpleModel` " +
            "\nBEGIN" +
            "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
            "\nINSERT INTO `TwoColumnModel`(`id`) VALUES(CAST(`new`.`name` AS INTEGER));" +
            "\nEND",
            createTrigger("MyTrigger").temporary().before() updateOn SimpleModel::class begin
                insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name")) and
                insert(TwoColumnModel::class)
                    .columnValues(id to Method.cast(NameAlias.ofTable("new", "name").property)
                        .`as`(SQLiteType.INTEGER)))

    }

    @Test
    fun validateTriggerWorks() {
        val trigger = createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
            insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name"))
        trigger.enable()
        SimpleModel("Test").insert()

        val result = select from TwoColumnModel::class where (name eq "Test")
        assertNotNull(result)
    }
}