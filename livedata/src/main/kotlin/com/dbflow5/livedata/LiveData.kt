package com.dbflow5.livedata

import androidx.lifecycle.LiveData
import com.dbflow5.config.FlowManager
import com.dbflow5.config.databaseForTable
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.extractFrom
import com.dbflow5.runtime.OnTableChangedListener
import com.dbflow5.runtime.TableNotifierRegister
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Return a new [LiveData] instance. Specify using the [evalFn] what query to run.
 */
fun <T : Any, Q : ModelQueriable<T>, R> Q.liveData(evalFn: (DatabaseWrapper, ModelQueriable<T>) -> R): LiveData<R> =
        QueryLiveData(this, evalFn)

class QueryLiveData<T : Any, R : Any?>(private val modelQueriable: ModelQueriable<T>,
                                       private val evalFn: (DatabaseWrapper, ModelQueriable<T>) -> R) : LiveData<R>() {
    private val register: TableNotifierRegister = FlowManager.newRegisterForTable(modelQueriable.table)

    private val associatedTables: Set<Class<*>> = modelQueriable.extractFrom()?.associatedTables
            ?: setOf(modelQueriable.table)

    private val onTableChangedListener = object : OnTableChangedListener {
        override fun onTableChanged(table: Class<*>?, action: ChangeAction) {
            if (table != null && associatedTables.contains(table)) {
                evaluateEmission(table.kotlin)
            }
        }
    }

    private fun evaluateEmission(table: KClass<*> = modelQueriable.table.kotlin) {
        databaseForTable(table)
                .beginTransactionAsync { evalFn(it, modelQueriable) }
                .execute { _, r -> value = r }
    }

    override fun onActive() {
        super.onActive()
        associatedTables.forEach { register.register(it) }
        register.setListener(onTableChangedListener)

        // trigger initial emission on active.
        evaluateEmission()
    }

    override fun onInactive() {
        super.onInactive()
        associatedTables.forEach { register.unregister(it) }
        register.setListener(null)
    }
}
