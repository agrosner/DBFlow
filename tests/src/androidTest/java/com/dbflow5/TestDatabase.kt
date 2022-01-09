package com.dbflow5

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.livedata.LiveDataModel
import com.dbflow5.migration.BaseMigration
import com.dbflow5.migration.FirstMigration
import com.dbflow5.migration.SecondMigration
import com.dbflow5.migration.UpdateTableMigration
import com.dbflow5.models.Account
import com.dbflow5.models.AllFieldsModel
import com.dbflow5.models.AllFieldsQueryModel
import com.dbflow5.models.Artist
import com.dbflow5.models.Author
import com.dbflow5.models.AuthorNameQuery
import com.dbflow5.models.AuthorView
import com.dbflow5.models.AutoIncrementingModel
import com.dbflow5.models.Blog
import com.dbflow5.models.BlogDeferred
import com.dbflow5.models.BlogPrimary
import com.dbflow5.models.BlogRef
import com.dbflow5.models.BlogRefNoModel
import com.dbflow5.models.BlogStubbed
import com.dbflow5.models.CharModel
import com.dbflow5.models.Coordinate
import com.dbflow5.models.Currency
import com.dbflow5.models.CustomBlobModel
import com.dbflow5.models.DefaultModel
import com.dbflow5.models.Dog
import com.dbflow5.models.DontAssignDefaultModel
import com.dbflow5.models.DontCreateModel
import com.dbflow5.models.EnumModel
import com.dbflow5.models.EnumTypeConverterModel
import com.dbflow5.models.FeedEntry
import com.dbflow5.models.Fts3Model
import com.dbflow5.models.Fts4Model
import com.dbflow5.models.Fts4VirtualModel2
import com.dbflow5.models.IndexModel
import com.dbflow5.models.InternalClass
import com.dbflow5.models.NonNullKotlinModel
import com.dbflow5.models.NonTypical.nonTypicalClassName
import com.dbflow5.models.NotNullReferenceModel
import com.dbflow5.models.NullableNumbers
import com.dbflow5.models.NumberModel
import com.dbflow5.models.OneToManyBaseModel
import com.dbflow5.models.OneToManyModel
import com.dbflow5.models.OrderCursorModel
import com.dbflow5.models.Outer
import com.dbflow5.models.Owner
import com.dbflow5.models.Path
import com.dbflow5.models.Position
import com.dbflow5.models.Position2
import com.dbflow5.models.PositionWithTypeConverter
import com.dbflow5.models.PriorityView
import com.dbflow5.models.ProspectQuiz
import com.dbflow5.models.ProspectQuizEntry
import com.dbflow5.models.SimpleCacheObject
import com.dbflow5.models.SimpleCustomModel
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleQuickCheckModel
import com.dbflow5.models.Song
import com.dbflow5.models.SqlListenerModel
import com.dbflow5.models.SubclassAllFields
import com.dbflow5.models.TempModel
import com.dbflow5.models.Transfer
import com.dbflow5.models.Transfer2
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TypeConverterModel
import com.dbflow5.models.UniqueModel
import com.dbflow5.models.UserInfo
import com.dbflow5.models.java.ExampleModel
import com.dbflow5.models.java.JavaModel
import com.dbflow5.models.java.JavaModelView
import com.dbflow5.rx2.query.SimpleRXModel

/**
 * Description:
 */
@Database(
    version = 1,
    tables = [
        ExampleModel::class,
        JavaModel::class,
        SimpleCacheObject::class,
        Coordinate::class,
        Path::class,
        Blog::class,
        Author::class,
        BlogDeferred::class,
        BlogRef::class,
        BlogRefNoModel::class,
        BlogPrimary::class,
        BlogStubbed::class,
        Position::class,
        Position2::class,
        PositionWithTypeConverter::class,
        NotNullReferenceModel::class,
        Outer.Inner::class,
        LiveDataModel::class,
        Artist::class,
        Song::class,
        IndexModel::class,
        AutoIncrementingModel::class,
        ProspectQuiz::class,
        ProspectQuizEntry::class,
        SimpleRXModel::class,
        SimpleModel::class,
        SimpleQuickCheckModel::class,
        NumberModel::class,
        CharModel::class,
        TwoColumnModel::class,
        DontCreateModel::class,
        EnumModel::class,
        AllFieldsModel::class,
        SubclassAllFields::class,
        DontAssignDefaultModel::class,
        OrderCursorModel::class,
        TypeConverterModel::class,
        EnumTypeConverterModel::class,
        FeedEntry::class,
        Transfer::class,
        Transfer2::class,
        Account::class,
        SqlListenerModel::class,
        DefaultModel::class,
        NullableNumbers::class,
        NonNullKotlinModel::class,
        Owner::class,
        Dog::class,
        Currency::class,
        UserInfo::class,
        InternalClass::class,
        UniqueModel::class,
        Fts3Model::class,
        Fts4Model::class,
        Fts4VirtualModel2::class,
        TempModel::class,
        nonTypicalClassName::class,
        OneToManyModel::class,
        OneToManyBaseModel::class,
    ],
    views = [
        JavaModelView::class,
        AuthorView::class,
        PriorityView::class,
    ],
    migrations = [
        FirstMigration::class,
        SecondMigration::class,
        TestDatabase.TestMigration::class,
        TestDatabase.SecondMigration::class,
    ],
    queries = [
        AuthorNameQuery::class,
        CustomBlobModel::class,
        AllFieldsQueryModel::class,
        SimpleCustomModel::class,
    ],
)
abstract class TestDatabase : DBFlowDatabase() {

    @Migration(version = 1, priority = 5)
    class TestMigration : UpdateTableMigration<SimpleModel>(SimpleModel::class) {
        override fun onPreMigrate() {
            super.onPreMigrate()
            set(SimpleModel_Table.name.eq("Test")).where(SimpleModel_Table.name.eq("Test1"))
        }
    }

    @Migration(version = 1, priority = 1)
    class SecondMigration : BaseMigration() {
        override fun migrate(database: DatabaseWrapper) {

        }
    }
}

@Database(
    version = 1, foreignKeyConstraintsEnforced = true,

    tables = [
        TestForeignKeyDatabase.SimpleModel::class,
        TestForeignKeyDatabase.SimpleForeignModel::class,
    ]
)
abstract class TestForeignKeyDatabase : DBFlowDatabase() {

    @Table
    data class SimpleModel(@PrimaryKey var name: String = "")

    @Table
    data class SimpleForeignModel(
        @PrimaryKey var id: Int = 0,
        @ForeignKey var model: SimpleModel? = null
    )
}
