package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.ColumnMap
import com.raizlabs.dbflow5.annotation.ModelView
import com.raizlabs.dbflow5.annotation.ModelViewQuery
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.models.Author_Table.*
import com.raizlabs.dbflow5.query.property.IProperty
import com.raizlabs.dbflow5.query.property.property
import com.raizlabs.dbflow5.query.select

class AuthorName(var name: String = "", var age: Int = 0)


@ModelView(database = TestDatabase::class)
class AuthorView(@Column var authorId: Int = 0, @Column var authorName: String = "",
                 @ColumnMap var author: AuthorName? = null) {

    companion object {
        @JvmStatic
        @ModelViewQuery
        fun getQuery() = (select(id.`as`("authorId"),
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
        fun getQuery() = select((first_name + last_name).`as`("name")) from Author::class
    }
}