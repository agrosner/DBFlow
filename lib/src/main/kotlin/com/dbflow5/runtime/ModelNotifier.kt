package com.dbflow5.runtime

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    val db: DatabaseWrapper

    fun <T : Any> notifyModelChanged(
        model: T,
        adapter: ModelAdapter<T>,
        action: ChangeAction
    )

    fun <T : Any> notifyTableChanged(
        table: KClass<T>,
        action: ChangeAction
    )

    fun newRegister(): TableNotifierRegister
}
