package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.ModelViewQuery
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.property
import com.raizlabs.android.dbflow.models.Author_Table.first_name
import com.raizlabs.android.dbflow.models.Author_Table.id
import com.raizlabs.android.dbflow.models.Author_Table.last_name
import com.raizlabs.android.dbflow.sql.language.SQLite.select
import com.raizlabs.android.dbflow.sql.language.property.IProperty

@ModelView(database = TestDatabase::class)
class AuthorView(@Column var authorId: Int = 0, @Column var authorName: String = "") {

    companion object {
        @JvmField
        @ModelViewQuery
        val query = select(id.`as`("authorId"),
            first_name.concatenate(" ".property as IProperty<out IProperty<*>>)
                .concatenate(last_name as IProperty<out IProperty<*>>).`as`("authorName")) from Author::class
    }
}