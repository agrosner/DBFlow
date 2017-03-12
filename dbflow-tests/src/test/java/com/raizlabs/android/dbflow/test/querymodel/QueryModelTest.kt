package com.raizlabs.android.dbflow.test.querymodel

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.sql.language.Where
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test
import java.util.UUID

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Description: Tests the [TestQueryModel] to ensure it works as expected.
 */
class QueryModelTest : FlowTestCase() {

    @Test
    fun testSalaryModel() {
        Delete.tables(SalaryModel::class.java)

        var salaryModel = SalaryModel()
        salaryModel.uid = UUID.randomUUID().toString()
        salaryModel.salary = 15000
        salaryModel.name = "Andrew Grosner"
        salaryModel.department = "Developer"
        salaryModel.save()

        salaryModel = SalaryModel()
        salaryModel.uid = UUID.randomUUID().toString()
        salaryModel.salary = 30000
        salaryModel.name = "Bill Gates"
        salaryModel.department = "Developer"
        salaryModel.save()

        val selectQuery = Select(SalaryModel_Table.department,
                SalaryModel_Table.salary.`as`("average_salary"),
                SalaryModel_Table.name.`as`("newName"))
                .from(SalaryModel::class.java).where().limit(1).groupBy(SalaryModel_Table.department)

        val testQueryModel = selectQuery.queryCustomSingle(TestQueryModel::class.java)!!

        assertTrue(testQueryModel.average_salary > 0)

        assertNotNull(testQueryModel.newName)

        assertNotNull(testQueryModel.department)
        assertEquals(testQueryModel.department, "Developer")

        val testQueryModels = selectQuery.queryCustomList(TestQueryModel::class.java)
        assertTrue(!testQueryModels.isEmpty())

        val model = testQueryModels[0]
        assertEquals(model, testQueryModel)


        Delete.tables(SalaryModel::class.java)
    }
}
