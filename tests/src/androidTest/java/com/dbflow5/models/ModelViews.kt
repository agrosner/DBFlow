package com.dbflow5.models

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.ModelViewQuery
import com.dbflow5.query.From
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.property
import com.dbflow5.query.select

class AuthorName(var name: String = "", var age: Int = 0)


@ModelView
class AuthorView(
    @Column var authorId: Int = 0, @Column var authorName: String = "",
    @ColumnMap var author: AuthorName? = null
) {

    companion object {
        @JvmStatic
        @ModelViewQuery
        fun getQuery(): From<Author> = (select(
            Author_Table.id.`as`("authorId"),
            Author_Table.first_name.concatenate(" ".property as IProperty<out IProperty<*>>)
                .concatenate(Author_Table.last_name as IProperty<out IProperty<*>>)
                .`as`("authorName")
        )
            from Author::class)
    }
}

@ModelView(priority = 2)
class PriorityView(var name: String = "") {

    companion object {
        @JvmStatic
        @get:ModelViewQuery
        val query: From<Author> =
            select((Author_Table.first_name + Author_Table.last_name).`as`("name")) from Author::class
    }
}
