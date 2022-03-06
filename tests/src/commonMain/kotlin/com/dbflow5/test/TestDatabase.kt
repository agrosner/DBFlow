package com.dbflow5.test

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.QueryAdapter
import com.dbflow5.adapter.ViewAdapter
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
        Fts3Model::class,
        Fts4ContentModel::class,
        Fts4VirtualModel::class,
        AutoIncrementingModel::class,
        Artist::class,
        Song::class,
    ],
    queries = [
        AuthorNameQuery::class,
    ],
    views = [
        AuthorView::class,
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

    abstract val authorViewAdapter: ViewAdapter<AuthorView>

    abstract val fts3ModelAdapter: ModelAdapter<Fts3Model>

    abstract val fts4ContentModelAdapter: ModelAdapter<Fts4ContentModel>

    abstract val fts4VirtualModelAdapter: ModelAdapter<Fts4VirtualModel>

    abstract val autoIncrementingModelAdapter: ModelAdapter<AutoIncrementingModel>

    abstract val artistAdapter: ModelAdapter<Artist>

    abstract val songAdapter: ModelAdapter<Song>

    abstract val artistSongAdapter: ModelAdapter<Artist_Song>
}

