package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.OrderBy
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.test.FlowTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Description: Tests enums
 */
class EnumModelTest : FlowTestCase() {

    @Test
    fun testEnumModel() {
        Delete.table(EnumModel::class.java)

        var enumModel = EnumModel()
        enumModel.difficulty = EnumModel.Difficulty.EASY
        enumModel.save()

        enumModel = Select().from(EnumModel::class.java).querySingle()!!
        assertEquals(EnumModel.Difficulty.EASY, enumModel.difficulty)

        Delete.table(EnumModel::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testEnumQuery_basicWhereEqual() {
        Delete.table(EnumModel::class.java)

        val easy = EnumModel()
        easy.difficulty = EnumModel.Difficulty.EASY
        easy.save()

        val medium = EnumModel()
        medium.difficulty = EnumModel.Difficulty.MEDIUM
        medium.save()

        val hard = EnumModel()
        hard.difficulty = EnumModel.Difficulty.HARD
        hard.save()

        val result = SQLite.select()
            .from(EnumModel::class.java)
            .where(EnumModel_Table.difficulty.eq(EnumModel.Difficulty.MEDIUM))
            .querySingle()

        assertNotNull(result)
        assertEquals(result!!.id, medium.id)

        Delete.table(EnumModel::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testEnumQuery_basicWhereIn() {
        Delete.table(EnumModel::class.java)

        val easy = EnumModel()
        easy.difficulty = EnumModel.Difficulty.EASY
        easy.save()

        val medium = EnumModel()
        medium.difficulty = EnumModel.Difficulty.MEDIUM
        medium.save()

        val hard = EnumModel()
        hard.difficulty = EnumModel.Difficulty.HARD
        hard.save()

        val result = SQLite.select()
            .from(EnumModel::class.java)
            .where(EnumModel_Table.difficulty.`in`(EnumModel.Difficulty.MEDIUM, EnumModel.Difficulty.HARD))
            .orderBy(OrderBy.fromProperty(EnumModel_Table.id).ascending())
            .queryList()

        assertNotNull(result)
        assertEquals(result[0].id, medium.id)
        assertEquals(result[1].id, hard.id)

        Delete.table(EnumModel::class.java)
    }
}
