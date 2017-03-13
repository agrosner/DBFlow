package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.sql.language.Method
import com.raizlabs.android.dbflow.sql.language.SQLite.select
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.sql.BoxedModel_Table.integerField
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description: Validates subquery formatting
 */
class SubqueryTest : FlowTestCase() {


    @Test
    fun testSubquery() {

        var query = (select
                from BoxedModel::class
                whereExists (select
                from BoxedModel::class
                where (integerField eq BoxedModel_Table.integerFieldNotNull)
                )).query

        assertEquals(
                "SELECT * FROM `BoxedModel` WHERE EXISTS (SELECT * FROM `BoxedModel` WHERE `integerField`=`integerFieldNotNull` )",
                query.trim())

        query = (select
                from BoxedModel::class
                where integerField.greaterThan(
                select(Method.avg(integerField))
                        from BoxedModel::class
                        where integerField.eq(integerField.withTable()))
                ).query

        assertEquals(
                "SELECT * FROM `BoxedModel` WHERE `integerField`>" + "(SELECT AVG(`integerField`) FROM `BoxedModel` WHERE `integerField`=`BoxedModel`.`integerField`)",
                query.trim())
    }
}
