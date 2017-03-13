package com.raizlabs.android.dbflow.container

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.FlowTestCase
import org.junit.Assert.*
import org.junit.Test

/**
 * Description: Asserts values are handled same for container and adapter
 */
class ContainerDifferenceTest : FlowTestCase() {

    @Test
    fun testContainer() {
        Delete.table(AutoIncrementContainer::class.java)

        var autoIncrementContainer = AIContainerForeign()
        autoIncrementContainer.a_id = 5
        autoIncrementContainer.name = "name"
        autoIncrementContainer.foreignModel = null
        autoIncrementContainer.container = null
        autoIncrementContainer.save()

        autoIncrementContainer = Select().from(AIContainerForeign::class.java)
            .where(AIContainerForeign_Table.id.`is`(autoIncrementContainer.id)).querySingle()
            ?: AIContainerForeign()
        assertNull(autoIncrementContainer.foreignModel)
        assertNull(autoIncrementContainer.container)

        val foreignModel = AutoIncrementContainer()
        foreignModel.a_id = 5
        foreignModel.name = "foreign"
        foreignModel.save()
        assertTrue(foreignModel.exists())
        autoIncrementContainer.foreignModel = foreignModel

        val foreignKeyContainer = AutoIncrementContainer()
        foreignKeyContainer.name = "container"
        foreignKeyContainer.a_id = 54
        foreignKeyContainer.save()
        assertTrue(foreignKeyContainer.exists())
        autoIncrementContainer.container = foreignKeyContainer

        foreignKeyContainer.save()
        assertTrue(foreignKeyContainer.exists())
        assertNotNull(autoIncrementContainer.container)
        assertNotNull(autoIncrementContainer.foreignModel)

        Delete.table(AutoIncrementContainer::class.java)
    }
}
