package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ColumnMap
import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.ModelViewQuery
import com.raizlabs.android.dbflow.models.Author_Table.*
import com.raizlabs.android.dbflow.query.property.IProperty
import com.raizlabs.android.dbflow.query.property.property
import com.raizlabs.android.dbflow.query.select
import com.raizlabs.android.dbflow.database.DatabaseWrapper

class AuthorName(var name: String = "", var age: Int = 0)


@ModelView(database = TestDatabase::class)
class AuthorView(@Column var authorId: Int = 0, @Column var authorName: String = "",
                 @ColumnMap var author: AuthorName? = null) {

    companion object {
        @JvmStatic
        @ModelViewQuery
        fun getQuery(wrapper: DatabaseWrapper) = (wrapper.select(id.`as`("authorId"),
                first_name.concatenate(" ".property as IProperty<out IProperty<*>>)
                        .concatenate(last_name as IProperty<out IProperty<*>>)
                        .`as`("authorName"))
                from Author::class)
    }
}

@ModelView(database = TestDatabase::class, priority = 2, allFields = true)
class PriorityView(var name: String = "") {

    companion object {
        @JvmStatic
        @ModelViewQuery
        fun getQuery(wrapper: DatabaseWrapper) =
                wrapper.select((first_name + last_name).`as`("name")) from Author::class
    }
}