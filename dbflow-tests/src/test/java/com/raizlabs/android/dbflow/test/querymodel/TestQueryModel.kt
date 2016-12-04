package com.raizlabs.android.dbflow.test.querymodel

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.structure.BaseQueryModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:
 */
@QueryModel(database = TestDatabase::class)
class TestQueryModel : BaseQueryModel() {

    @Column
    var newName: String? = null

    @Column
    var average_salary: Long = 0

    @Column
    var department: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val that = o as TestQueryModel?

        if (average_salary != that!!.average_salary) {
            return false
        }
        if (if (newName != null) newName != that.newName else that.newName != null) {
            return false
        }
        return !if (department != null) department != that.department else that.department != null

    }

    override fun hashCode(): Int {
        var result = if (newName != null) newName!!.hashCode() else 0
        result = 31 * result + (average_salary xor average_salary.ushr(32)).toInt()
        result = 31 * result + if (department != null) department!!.hashCode() else 0
        return result
    }
}
