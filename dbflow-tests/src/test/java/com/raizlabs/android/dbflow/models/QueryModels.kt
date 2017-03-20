package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.QueryModel

@QueryModel(database = TestDatabase::class)
class AuthorNameQuery(@Column var blogName: String = "",
                      @Column var authorId: Int = 0, @Column var blogId: Int = 0)

