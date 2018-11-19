package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.ModelViewQuery
import com.dbflow5.models.Author_Table.*
import com.dbflow5.query.From
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.property
import com.dbflow5.query.select

class AuthorName(var name: String = "", var age: Int = 0)


@ModelView(database = TestDatabase::class)
class AuthorView(@Column var authorId: Int = 0, @Column var authorName: String = "",
                 @ColumnMap var author: AuthorName? = null) {

    companion object {
        @JvmStatic
        @ModelViewQuery
        fun getQuery(): From<Author> = (select(id.`as`("authorId"),
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
        fun getQuery(): From<Author> = select((first_name + last_name).`as`("name")) from Author::class
    }
}