package com.dbflow5.runtime

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.structure.ChangeAction

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>, action: ChangeAction)

    fun <T : Any> notifyTableChanged(table: Class<T>, action: ChangeAction)

    fun newRegister(): TableNotifierRegister
}
