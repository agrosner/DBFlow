package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.requireCustomSingle
import com.dbflow5.query.select
import com.dbflow5.structure.save
import org.junit.Test
import kotlin.test.assertEquals

class OneToManyModelTest : BaseUnitTest() {

    @Test
    fun testOneToManyModel() {
        database<TestDatabase> { db ->
            OneToManyModel(
                name = "name"
            ).save(db)

            OneToManyBaseModel(
                id = 1,
                parentName = "name"
            ).save(db)

            val items =
                (select from OneToManyModel::class).requireCustomSingle<OneToManyModel_OneToManyBaseModel>(
                    db
                )
            assertEquals(items.children.size, 1)
        }
    }
}