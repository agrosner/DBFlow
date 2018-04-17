package com.raizlabs.dbflow5.runtime

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>, action: ChangeAction)

    fun <T : Any> notifyTableChanged(table: KClass<T>, action: ChangeAction)

    fun newRegister(): TableNotifierRegister
}
