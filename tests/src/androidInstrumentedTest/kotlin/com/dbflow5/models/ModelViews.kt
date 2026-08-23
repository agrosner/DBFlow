package com.dbflow5.models

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ModelView

class AuthorName(var name: String = "", var age: Int = 0)

@ModelView("SELECT `id` AS `authorId`, `first_name` || ' ' || `last_name` AS `authorName` FROM `Author`")
class AuthorView(
    @Column var authorId: Int = 0, @Column var authorName: String = "",
    @ColumnMap var author: AuthorName? = null
)

@ModelView(priority = 2, query = "SELECT (`first_name` + `last_name`) AS `name` FROM `Author`")
class PriorityView(var name: String = "")
