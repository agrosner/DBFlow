package com.raizlabs.android.dbflow.test.contentobserver

import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.language.SQLCondition
import com.raizlabs.android.dbflow.structure.BaseModel
import java.util.concurrent.Callable

/**
 * Description:
 */
class MockOnModelStateChangedListener : FlowContentObserver.OnModelStateChangedListener {

    val methodcalled = arrayOf(false, false, false, false)
    val methodCalls: Array<Callable<Boolean>?> = arrayOfNulls(4)
    val conditions: Array<Array<SQLCondition>> = arrayOf()

    init {
        for (i in methodCalls.indices) {
            val finalI = i
            methodCalls[i] = Callable { methodcalled[finalI] }
        }
    }

    override fun onModelStateChanged(table: Class<*>?, action: BaseModel.Action,
                                     primaryKeyValues: Array<SQLCondition>) {
        when (action) {
            BaseModel.Action.CHANGE -> methodCalls.indices.forEach { i ->
                try {
                    methodcalled[i] = true
                    conditions[i] = primaryKeyValues
                    methodCalls[i]!!.call()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            BaseModel.Action.SAVE -> try {
                conditions[2] = primaryKeyValues
                methodcalled[2] = true
                methodCalls[2]!!.call()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            BaseModel.Action.DELETE -> try {
                conditions[3] = primaryKeyValues
                methodcalled[3] = true
                methodCalls[3]!!.call()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            BaseModel.Action.INSERT -> try {
                conditions[0] = primaryKeyValues
                methodcalled[0] = true
                methodCalls[0]!!.call()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            BaseModel.Action.UPDATE -> try {
                conditions[1] = primaryKeyValues
                methodcalled[1] = true
                methodCalls[1]!!.call()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}
