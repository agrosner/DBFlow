package com.raizlabs.android.dbflow.test.structure.join

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.From
import com.raizlabs.android.dbflow.sql.language.Join
import com.raizlabs.android.dbflow.sql.language.Method
import com.raizlabs.android.dbflow.sql.language.NameAlias
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.sql.DefaultModel
import com.raizlabs.android.dbflow.test.sql.DefaultModel_Table
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table

import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * Description:
 */
class JoinTest : FlowTestCase() {


    @Test
    fun testJoins() {
        Delete.tables(Company::class.java, Department::class.java)

        var company = Company()
        company.id = 1
        company.name = "Paul"
        company.age = 32
        company.address = "California"
        company.salary = 20000.0
        company.insert()

        company = Company()
        company.id = 2
        company.name = "Allen"
        company.age = 25
        company.address = "Texas"
        company.salary = 15000.0
        company.insert()

        company = Company()
        company.id = 3
        company.name = "Teddy"
        company.age = 23
        company.address = "Norway"
        company.salary = 20000.0
        company.insert()

        company = Company()
        company.id = 4
        company.name = "Mark"
        company.age = 25
        company.address = "Rich-Mond"
        company.salary = 65000.0
        company.insert()

        company = Company()
        company.id = 5
        company.name = "David"
        company.age = 27
        company.address = "Texas"
        company.salary = 85000.0
        company.insert()

        company = Company()
        company.id = 6
        company.name = "Kim"
        company.age = 22
        company.address = "South-Hall"
        company.salary = 45000.0
        company.insert()

        company = Company()
        company.id = 7
        company.name = "James"
        company.age = 24
        company.address = "Houston"
        company.salary = 10000.0
        company.insert()

        assertEquals(SQLite.select(Method.count()).from(Company::class.java).count(), 7)

        var department = Department()
        department.id = 1
        department.dept = "IT Billing"
        department.emp_id = 1
        department.insert()

        department = Department()
        department.id = 2
        department.dept = "Engineering"
        department.emp_id = 2
        department.insert()

        department = Department()
        department.id = 3
        department.dept = "Finance"
        department.emp_id = 7
        department.insert()

        assertEquals(SQLite.select(Method.count()).from(Department::class.java).count(), 3)

        val joinQuery = SQLite.select(Department_Table.emp_id.withTable(), Company_Table.name, Department_Table.dept)
                .from(Company::class.java)
                .join(Department::class.java, Join.JoinType.INNER)
                .on(Company_Table.id.withTable().eq(Department_Table.emp_id.withTable()))
        val query = joinQuery.query

        assertEquals("SELECT `Department`.`emp_id`,`name`,`dept` FROM `Company` INNER JOIN `Department` " + "ON `Company`.`id`=`Department`.`emp_id`", query.trim { it <= ' ' })

        val companyDepartmentJoins = joinQuery.queryCustomList(CompanyDepartmentJoin::class.java)

        assertEquals(companyDepartmentJoins.size.toLong(), 3)

        var departmentJoin = companyDepartmentJoins[0]
        assertEquals(departmentJoin.dept, "IT Billing")
        assertEquals(departmentJoin.emp_id, 1)
        assertEquals(departmentJoin.name, "Paul")

        departmentJoin = companyDepartmentJoins[1]
        assertEquals(departmentJoin.dept, "Engineering")
        assertEquals(departmentJoin.emp_id, 2)
        assertEquals(departmentJoin.name, "Allen")

        departmentJoin = companyDepartmentJoins[2]
        assertEquals(departmentJoin.dept, "Finance")
        assertEquals(departmentJoin.emp_id, 7)
        assertEquals(departmentJoin.name, "James")

        Delete.tables(Company::class.java, Department::class.java)
    }

    @Test
    fun test_queryJoinValidation() {
        val query = SQLite.select()
                .from(TestModel1::class.java)
                .innerJoin(
                        SQLite.select(DefaultModel_Table.count,
                                Method.max(DefaultModel_Table.name).`as`("MaxName"))
                                .from(DefaultModel::class.java)
                                .groupBy(DefaultModel_Table.date)).`as`("topmsg")
                .on(TestModel1_Table.name.withTable().eq(
                        PropertyFactory.from<Any>(null, "`MaxName`").withTable(NameAlias.builder("topmsg").build()))).query

        assertEquals("SELECT * FROM `TestModel1`" +
                " INNER JOIN (SELECT `count`,MAX(`name`) AS `MaxName`" +
                " FROM `DefaultModel`" +
                " GROUP BY `date`) AS `topmsg`" +
                " ON `TestModel1`.`name`=`topmsg`.`MaxName`",
                query.trim { it <= ' ' })
    }
}
