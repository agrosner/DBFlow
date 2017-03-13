package com.raizlabs.android.dbflow.sql.index

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Index
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty
import com.raizlabs.android.dbflow.FlowTestCase

import org.junit.Test

import com.raizlabs.android.dbflow.sql.index.IndexModel_Table.salary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class IndexTest : FlowTestCase() {

    @Test
    fun testIndex() {

        Delete.table(IndexModel::class.java)

        val modelIndex = Index<IndexModel>("salary_index")
                .on(IndexModel::class.java, salary)
        modelIndex.disable()

        assertEquals("CREATE INDEX IF NOT EXISTS `salary_index` ON `IndexModel`(`salary`)", modelIndex.query.trim { it <= ' ' })

        modelIndex.enable()

        var indexModel = IndexModel()
        indexModel.name = "Index"
        indexModel.salary = 30000
        indexModel.save()

        indexModel = IndexModel()
        indexModel.name = "Index2"
        indexModel.salary = 15000
        indexModel.save()

        val indexProperty = IndexProperty(modelIndex.indexName,
                true, IndexModel::class.java, salary)

        val list = Select().from(IndexModel::class.java)
                .indexedBy(indexProperty)
                .where(salary.greaterThan(20000L)).queryList()

        assertTrue(list.size == 1)

        modelIndex.disable()

        Delete.table(IndexModel::class.java)

    }
}

