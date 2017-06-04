package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.converter.TypeConverter
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.listener.SQLiteStatementListener
import java.util.*


/**
 * Description:
 */
@Table(database = TestDatabase::class)
class SimpleModel(@PrimaryKey var name: String? = "")

@QueryModel(database = TestDatabase::class)
class SimpleCustomModel(@Column var name: String? = "")

@Table(database = TestDatabase::class, insertConflict = ConflictAction.FAIL, updateConflict = ConflictAction.FAIL)
class NumberModel(@PrimaryKey var id: Int = 0)

@Table(database = TestDatabase::class)
class CharModel(@PrimaryKey var id: Int = 0, @Column var exampleChar: Char? = null)

@Table(database = TestDatabase::class)
class TwoColumnModel(@PrimaryKey var name: String? = "", @Column var id: Int = 0)

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

@Table(database = TestDatabase::class)
class EnumModel(@PrimaryKey var id: Int = 0, @Column var difficulty: Difficulty? = Difficulty.EASY)

@Table(database = TestDatabase::class, allFields = true)
open class AllFieldsModel(@PrimaryKey var name: String? = null,
                          var count: Int? = 0,
                          @Column(getterName = "getTruth")
                          var truth: Boolean = false,
                          internal val finalName: String = "",
                          @ColumnIgnore private val hidden: Int = 0) {

    companion object {

        // example field to ensure static not used.
        var COUNT: Int = 0
    }
}

@Table(database = TestDatabase::class, allFields = true)
class SubclassAllFields(@Column var order: Int = 0) : AllFieldsModel()

@Table(database = TestDatabase::class, assignDefaultValuesFromCursor = false)
class DontAssignDefaultModel(@PrimaryKey var name: String? = null,
                             @Column(getterName = "getNullableBool") var nullableBool: Boolean? = null,
                             @Column var index: Int = 0)

@Table(database = TestDatabase::class, orderedCursorLookUp = true)
class OrderCursorModel(@PrimaryKey var id: Int = 0, @Column var name: String? = "",
                       @Column var age: Int = 0)

@Table(database = TestDatabase::class)
class TypeConverterModel(@PrimaryKey var id: Int = 0,
                         @Column var blob: Blob? = null,
                         @Column(typeConverter = CustomTypeConverter::class)
                         @PrimaryKey var customType: CustomType? = null)

@Table(database = TestDatabase::class, allFields = true)
class FeedEntry(@PrimaryKey var id: Int = 0,
                var title: String? = null,
                var subtitle: String? = null)

@Table(database = TestDatabase::class)
@ManyToMany(
        generatedTableClassName = "Refund", referencedTable = Transfer::class,
        referencedTableColumnName = "refund_in", thisTableColumnName = "refund_out",
        saveForeignKeyModels = true
)
data class Transfer(@PrimaryKey var transfer_id: UUID = UUID.randomUUID())

@Table(database = TestDatabase::class)
data class Transfer2(
        @PrimaryKey
        var id: UUID = UUID.randomUUID(),
        @ForeignKey(stubbedRelationship = true)
        var origin: Account? = null
)

@Table(database = TestDatabase::class)
data class Account(@PrimaryKey var id: UUID = UUID.randomUUID())

@Table(database = TestDatabase::class)
class SqlListenerModel(@PrimaryKey var id: Int = 0) : SQLiteStatementListener {
    override fun onBindToStatement(databaseStatement: DatabaseStatement) {
        TODO("not implemented")
    }

    override fun onBindToInsertStatement(databaseStatement: DatabaseStatement) {
        TODO("not implemented")
    }

    override fun onBindToUpdateStatement(databaseStatement: DatabaseStatement) {
        TODO("not implemented")
    }

    override fun onBindToDeleteStatement(databaseStatement: DatabaseStatement) {
        TODO("not implemented")
    }
}

class CustomType(var name: String? = "")

class CustomTypeConverter : TypeConverter<String, CustomType>() {
    override fun getDBValue(model: CustomType?) = model?.name

    override fun getModelValue(data: String?) = if (data == null) {
        null
    } else {
        CustomType(data)
    }

}

@Table(database = TestDatabase::class)
class DefaultModel(@PrimaryKey @Column(defaultValue = "5") var id: Int? = 0,
                   @Column(defaultValue = "5.0") var location: Double? = 0.0,
                   @Column(defaultValue = "\"String\"") var name: String? = "")

@Table(database = TestDatabase::class, cachingEnabled = true)
class TestModelChild : BaseModel() {
    @PrimaryKey
    var id: Long = 0

    @Column
    var name: String? = null
}

@Table(database = TestDatabase::class)
class TestModelParent : BaseModel() {
    @PrimaryKey
    var id: Long = 0

    @Column
    var name: String? = null

    @ForeignKey(stubbedRelationship = true)
    var child: TestModelChild? = null
}

@Table(database = TestDatabase::class)
class NonNullKotlinModel(@PrimaryKey var name: String = "",
                         @Column var date: Date = Date())