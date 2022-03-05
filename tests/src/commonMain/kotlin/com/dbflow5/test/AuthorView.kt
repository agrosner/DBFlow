package com.dbflow5.test

import com.dbflow5.annotation.ModelView

@ModelView("SELECT `id` AS `authorId`, `first_name` || ' ' || `last_name` AS `authorName` FROM `Author`")
data class AuthorView(
    val authorId: Int,
    val authorName: String,
)
