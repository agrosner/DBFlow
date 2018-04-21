package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.query.property.IProperty

/**
 * Description:
 */
actual class Trigger
actual constructor(name: String) : InternalTrigger(name) {

    fun <TModel : Any> deleteOn(onTable: Class<TModel>): TriggerMethod<TModel> = deleteOn(onTable.kotlin)

    fun <TModel : Any> insertOn(onTable: Class<TModel>): TriggerMethod<TModel> = insertOn(onTable.kotlin)

    fun <TModel : Any> updateOn(onTable: Class<TModel>, vararg properties: IProperty<*>): TriggerMethod<TModel> =
        TriggerMethod(this, TriggerMethod.UPDATE, onTable.kotlin, *properties)

    infix fun <T : Any> updateOn(onTable: Class<T>): TriggerMethod<T> =
        TriggerMethod(this, TriggerMethod.UPDATE, onTable.kotlin)

    actual companion object {

        /**
         * @param triggerName The name of the trigger to use.
         * @return A new trigger.
         */
        @JvmStatic
        actual fun create(triggerName: String) = Trigger(triggerName)
    }
}