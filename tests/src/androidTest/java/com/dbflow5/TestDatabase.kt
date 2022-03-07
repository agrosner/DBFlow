package com.dbflow5

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.QueryAdapter
import com.dbflow5.adapter.ViewAdapter
import com.dbflow5.adapter.migrationAdapter
import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Migration
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.scope.MigrationScope
import com.dbflow5.livedata.LiveDataModel
import com.dbflow5.migration.FirstMigration
import com.dbflow5.migration.SecondMigration
import com.dbflow5.models.Account
import com.dbflow5.models.AllFieldsModel
import com.dbflow5.models.AllFieldsQueryModel
import com.dbflow5.models.Artist
import com.dbflow5.models.Artist_Song
import com.dbflow5.models.Author
import com.dbflow5.models.AuthorNameQuery
import com.dbflow5.models.AuthorView
import com.dbflow5.test.usecases.AutoIncrementingModel
import com.dbflow5.models.Blog
import com.dbflow5.models.BlogDeferred
import com.dbflow5.models.BlogPrimary
import com.dbflow5.models.BlogRef
import com.dbflow5.models.BlogRefNoModel
import com.dbflow5.models.BlogStubbed
import com.dbflow5.models.CharModel
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
import com.dbflow5.test.IndexModel
import com.dbflow5.models.InternalClass
import com.dbflow5.models.NonNullKotlinModel
import com.dbflow5.test.NonTypical.nonTypicalClassName
import com.dbflow5.models.NotNullReferenceModel
import com.dbflow5.models.NullableNumbers
import com.dbflow5.models.NumberModel
import com.dbflow5.models.OneToManyBaseModel
import com.dbflow5.models.OneToManyModel
import com.dbflow5.models.OrderCursorModel
import com.dbflow5.models.Outer
import com.dbflow5.models.Owner
import com.dbflow5.models.Position
import com.dbflow5.models.Position2
import com.dbflow5.models.PositionWithTypeConverter
import com.dbflow5.models.PriorityView
import com.dbflow5.models.ProspectQuiz
import com.dbflow5.models.ProspectQuizEntry
import com.dbflow5.models.SimpleCustomModel
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleQuickCheckModel
import com.dbflow5.models.Song
import com.dbflow5.models.SqlListenerModel
import com.dbflow5.models.SubclassAllFields
import com.dbflow5.test.usecases.TempModel
import com.dbflow5.models.Transfer
import com.dbflow5.models.Transfer2
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TypeConverterModel
import com.dbflow5.models.UniqueModel
import com.dbflow5.models.UserInfo
import com.dbflow5.models.java.ExampleModel
import com.dbflow5.models.java.JavaModel
import com.dbflow5.models.java.JavaModelView
import com.dbflow5.query.operations.sqlLiteralOf
import com.dbflow5.query.update
import com.dbflow5.rx2.query.SimpleRXModel

/**
 * Description:
 */
@Database(
    version = 1,
    tables = [
        ExampleModel::class,
        JavaModel::class,
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
        com.dbflow5.test.NonTypical.nonTypicalClassName::class,
        OneToManyModel::class,
        OneToManyBaseModel::class,
        com.dbflow5.test.sql.language.CaseModel::class,
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

    abstract val exampleAdapter: ModelAdapter<ExampleModel>
    abstract val javaModelAdapter: ModelAdapter<JavaModel>
    abstract val blogAdapter: ModelAdapter<Blog>
    abstract val authorAdapter: ModelAdapter<Author>
    abstract val blogDeferredAdapter: ModelAdapter<BlogDeferred>
    abstract val blogRefAdapter: ModelAdapter<BlogRef>
    abstract val blogRefNoModelAdapter: ModelAdapter<BlogRefNoModel>
    abstract val blogPrimaryAdapter: ModelAdapter<BlogPrimary>
    abstract val blogStubbedAdapter: ModelAdapter<BlogStubbed>
    abstract val positionAdapter: ModelAdapter<Position>
    abstract val position2Adapter: ModelAdapter<Position2>
    abstract val positionWithTypeConverterAdapter: ModelAdapter<PositionWithTypeConverter>
    abstract val notNullReferenceModelAdapter: ModelAdapter<NotNullReferenceModel>
    abstract val outerInnerAdapter: ModelAdapter<Outer.Inner>
    abstract val liveDataModelAdapter: ModelAdapter<LiveDataModel>
    abstract val artistAdapter: ModelAdapter<Artist>
    abstract val songAdapter: ModelAdapter<Song>
    abstract val artistSongAdapter: ModelAdapter<Artist_Song>
    abstract val indexModelAdapter: ModelAdapter<IndexModel>
    abstract val autoIncrementingModelAdapter: ModelAdapter<AutoIncrementingModel>

    abstract val caseAdapter: ModelAdapter<com.dbflow5.test.sql.language.CaseModel>

    // TODO: internal support in KAPT.
    // internal abstract val prospectQuizAdapter: ModelAdapter<ProspectQuiz>
    // internal abstract val prospectQuizEntryAdapter: ModelAdapter<ProspectQuizEntry>
    abstract val simpleRXModelAdapter: ModelAdapter<SimpleRXModel>
    abstract val simpleModelAdapter: ModelAdapter<SimpleModel>
    abstract val simpleQuickCheckModelAdapter: ModelAdapter<SimpleQuickCheckModel>
    abstract val numberModelAdapter: ModelAdapter<NumberModel>
    abstract val charModelAdapter: ModelAdapter<CharModel>
    abstract val twoColumnModelAdapter: ModelAdapter<TwoColumnModel>
    abstract val dontCreateModelAdapter: ModelAdapter<DontCreateModel>
    abstract val enumModelAdapter: ModelAdapter<EnumModel>
    abstract val allFieldsModelAdapter: ModelAdapter<AllFieldsModel>
    abstract val subclassAllFieldsAdapter: ModelAdapter<SubclassAllFields>
    abstract val dontAssignDefaultModelAdapter: ModelAdapter<DontAssignDefaultModel>
    abstract val orderCursorModelAdapter: ModelAdapter<OrderCursorModel>
    abstract val typeConverterModelAdapter: ModelAdapter<TypeConverterModel>
    abstract val enumTypeConverterModelAdapter: ModelAdapter<EnumTypeConverterModel>
    abstract val feedEntryAdapter: ModelAdapter<FeedEntry>
    abstract val transferAdapter: ModelAdapter<Transfer>
    abstract val transfer2Adapter: ModelAdapter<Transfer2>
    abstract val accountAdapter: ModelAdapter<Account>
    abstract val sqlListenerModelAdapter: ModelAdapter<SqlListenerModel>
    abstract val defaultModelAdapter: ModelAdapter<DefaultModel>
    abstract val nullableNumbersAdapter: ModelAdapter<NullableNumbers>
    abstract val nonNullKotlinModelAdapter: ModelAdapter<NonNullKotlinModel>
    abstract val ownerAdapter: ModelAdapter<Owner>
    abstract val dogAdapter: ModelAdapter<Dog>
    abstract val currencyAdapter: ModelAdapter<Currency>
    abstract val userInfoAdapter: ModelAdapter<UserInfo>
    abstract val internalClassAdapter: ModelAdapter<InternalClass>
    abstract val uniqueModelAdapter: ModelAdapter<UniqueModel>
    abstract val fts3ModelAdapter: ModelAdapter<Fts3Model>
    abstract val fts4ModelAdapter: ModelAdapter<Fts4Model>
    abstract val fts4VirtualModel2Adapter: ModelAdapter<Fts4VirtualModel2>
    abstract val tempModelAdapter: ModelAdapter<TempModel>
    abstract val nonTypicalClassNameAdapter: ModelAdapter<com.dbflow5.test.NonTypical.nonTypicalClassName>
    abstract val oneToManyModelAdapter: ModelAdapter<OneToManyModel>
    abstract val oneToManyBaseModelAdapter: ModelAdapter<OneToManyBaseModel>


    abstract val javaModelViewAdapter: ViewAdapter<JavaModelView>
    abstract val authorViewAdapter: ViewAdapter<AuthorView>
    abstract val priorityViewAdapter: ViewAdapter<PriorityView>

    abstract val authorNameQuery: QueryAdapter<AuthorNameQuery>
    abstract val customBlobModel: QueryAdapter<CustomBlobModel>
    abstract val allFieldsQueryModel: QueryAdapter<AllFieldsQueryModel>
    abstract val simpleCustomModel: QueryAdapter<SimpleCustomModel>

    @Migration(version = 1, priority = 5)
    class TestMigration : com.dbflow5.database.Migration {
        override suspend fun MigrationScope.migrate(database: DatabaseWrapper) {
            migrationAdapter("SimpleModel").update()
                .set(name.eq("Test"))
                .where(name.eq("Test1"))
                .execute(database)
        }

        private companion object {
            val name = sqlLiteralOf("name")
        }
    }

    @Migration(version = 1, priority = 1)
    class SecondMigration : com.dbflow5.database.Migration {
        override suspend fun MigrationScope.migrate(database: DatabaseWrapper) {

        }
    }

}
