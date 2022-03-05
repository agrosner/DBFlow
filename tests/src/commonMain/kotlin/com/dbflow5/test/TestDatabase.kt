package com.dbflow5.test

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.QueryAdapter
import com.dbflow5.annotation.Database
import com.dbflow5.config.DBFlowDatabase

@Database(
    tables = [
        Author::class,
        SimpleModel::class,
        CaseModel::class,
        TwoColumnModel::class,
        EnumTypeConverterModel::class,
        NumberModel::class,
        TypeConverterModel::class,
        Blog::class,
    ],
    queries = [
        AuthorNameQuery::class,
    ],
    version = 1,
)
abstract class TestDatabase : DBFlowDatabase() {

    abstract val simpleModelAdapter: ModelAdapter<SimpleModel>

    abstract val twoColumnModelAdapter: ModelAdapter<TwoColumnModel>

    abstract val numberModelAdapter: ModelAdapter<NumberModel>

    abstract val authorAdapter: ModelAdapter<Author>

    abstract val authorNameQueryAdapter: QueryAdapter<AuthorNameQuery>

    abstract val blogAdapter: ModelAdapter<Blog>
}
