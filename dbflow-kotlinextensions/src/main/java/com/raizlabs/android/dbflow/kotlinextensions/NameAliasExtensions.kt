package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.NameAlias

val String.nameAlias
    get() = NameAlias.of(this)

fun String.nameAlias(alias: String? = null) = NameAlias.of(this, alias)

