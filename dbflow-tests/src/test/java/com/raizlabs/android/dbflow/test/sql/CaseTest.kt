package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.sql.language.BaseQueriable
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Method
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.test.FlowTestCase
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class CaseTest : FlowTestCase() {

    @Test
    fun test_SQL() {
        Delete.table(CaseModel::class.java)

        var caseModel = CaseModel()
        caseModel.customerId = 505
        caseModel.firstName = "Andrew"
        caseModel.lastName = "Grosner"
        caseModel.country = "USA"
        caseModel.insert()

        caseModel = CaseModel()
        caseModel.customerId = 506
        caseModel.firstName = "Andrew"
        caseModel.lastName = "Grosners"
        caseModel.country = "Canada"
        caseModel.insert()

        var queriable: BaseQueriable<CaseModel> = SQLite.select(CaseModel_Table.customerId,
            CaseModel_Table.firstName,
            CaseModel_Table.lastName,
            SQLite.caseWhen<Any>(CaseModel_Table.country.eq("USA"))
                .then("Domestic")
                ._else("Foreign").end("CustomerGroup")).from(CaseModel::class.java)

        assertEquals("SELECT `customerId`,`firstName`,`lastName`, CASE WHEN `country`='USA' " + "THEN 'Domestic' ELSE 'Foreign' END `CustomerGroup` FROM `CaseModel`", queriable.query.trim { it <= ' ' })

        queriable = SQLite.select(CaseModel_Table.customerId,
            CaseModel_Table.firstName,
            CaseModel_Table.lastName,
            SQLite._case(CaseModel_Table.country)
                .`when`("USA")
                .then("Domestic")
                ._else("Foreign").end("CustomerGroup")).from(CaseModel::class.java)

        assertEquals("SELECT `customerId`,`firstName`,`lastName`, CASE `country` WHEN 'USA' " + "THEN 'Domestic' ELSE 'Foreign' END `CustomerGroup` FROM `CaseModel`", queriable.getQuery().trim { it <= ' ' })


        Delete.table(CaseModel::class.java)
    }

    // TODO: restore
    /* @Test
     @Throws(Exception::class)
     fun test_MuliWhenCase_SQL() {
         Delete.table(CaseModel::class.java)

         var caseModel = CaseModel()
         caseModel.customerId = 507
         caseModel.firstName = "Namey"
         caseModel.lastName = "McNameFace"
         caseModel.country = "IO2016"
         caseModel.insert()

         caseModel = CaseModel()
         caseModel.customerId = 508
         caseModel.firstName = "Schooly"
         caseModel.lastName = "McSchoolFace"
         caseModel.country = "RepublicOfTexas"
         caseModel.insert()

         val queriable = SQLite.select()
             .from(CaseModel::class.java)
             .where(SQLite.caseWhen<Any>(CaseModel_Table.customerId.greaterThan(507))
                 .then(CaseModel_Table.firstName.like("School"))
                 .`when`(CaseModel_Table.customerId.greaterThan(508))
                 .then(CaseModel_Table.firstName.like("Name"))
                 .endAsCondition())

         assertEquals("SELECT * FROM `CaseModel` WHERE  " +
             "CASE WHEN `customerId`>507 " +
             "THEN `firstName` LIKE 'School'  " +
             "WHEN `customerId`>508 " +
             "THEN `firstName` LIKE 'Name' END", queriable.query.trim { it <= ' ' })
     }*/

    @Test
    fun test_caseProperty() {
        val query = SQLite._case(CaseModel_Table.country)
            .`when`(CaseModel_Table.firstName)
            .then(CaseModel_Table.lastName).query
        assertEquals("CASE `country` WHEN `firstName` THEN `lastName`", query.trim { it <= ' ' })
    }

    @Test
    fun test_emptyEndCase() {
        val query = Method.count(SQLite._case(CaseModel_Table.country)
            .`when`(CaseModel_Table.firstName)
            .then(CaseModel_Table.lastName).end()).query
        assertEquals("COUNT( CASE `country` WHEN `firstName` THEN `lastName` END )", query.trim { it <= ' ' })

    }
}
