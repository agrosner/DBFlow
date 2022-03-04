package com.dbflow5.test

import com.dbflow5.database.config.DBCreator
import com.dbflow5.database.config.DBPlatformSettings
import com.dbflow5.database.config.DBSettings
import com.dbflow5.test.helpers.platformSettings
import kotlin.test.Test

class SimpleModelTest {

    val dbRule = DatabaseTestRule(creator = object : DBCreator<TestDatabase> {
        override fun create(
            platformSettings: DBPlatformSettings,
            settingsFn: DBSettings.() -> DBSettings
        ): TestDatabase = TestDatabase_Database.create(platformSettings())
    })

    @Test
    fun canSaveSimpleModel() = dbRule.runTest {
        val model = SimpleModel(
            id = "5"
        )
        simpleModelAdapter.save(model)
    }
}
