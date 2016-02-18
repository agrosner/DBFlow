package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.sql.language.property.BaseProperty
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: A file containing extensions for adding query syntactic sugar.
 */


fun <TModel : Model> select(vararg property: IProperty<out IProperty<*>>, init: Select.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> {
    var select = SQLite.select(*property)
    return init(select)
}

fun <TModel : Model> from(tableClass: Class<TModel>, )

