package com.dbflow5.test

import com.dbflow5.annotation.Query

@Query
data class AuthorNameQuery(
    val blogName: String,
    val authorId: Int,
    val blogId: Int,
)

