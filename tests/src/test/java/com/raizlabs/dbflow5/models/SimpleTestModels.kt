package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.ColumnIgnore
import com.raizlabs.dbflow5.annotation.ConflictAction
import com.raizlabs.dbflow5.annotation.ForeignKey
import com.raizlabs.dbflow5.annotation.ManyToMany
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.QueryModel
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.converter.TypeConverter
import com.raizlabs.dbflow5.data.Blob
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.query.SQLiteStatementListener
import com.raizlabs.dbflow5.structure.BaseModel
import java.math.BigDecimal
import java.math.BigInteger
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
class TwoColumnModel(@PrimaryKey var name: String? = "", @Column(defaultValue = "56") var id: Int = 0)

@Table(database = TestDatabase::class, createWithDatabase = false)
class DontCreateModel(@PrimaryKey var id: Int = 0)

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
                         @Column(typeConverter = BlobConverter::class) var opaqueData: ByteArray? = null,
                         @Column var blob: Blob? = null,
                         @Column(typeConverter = CustomTypeConverter::class)
                         @PrimaryKey var customType: CustomType? = null)

@Table(database = TestDatabase::class)
class EnumTypeConverterModel(@PrimaryKey var id: Int = 0,
                             @Column var blob: Blob? = null,
                             @Column var byteArray: ByteArray? = null,
                             @Column(typeConverter = CustomEnumTypeConverter::class)
                             var difficulty: Difficulty = Difficulty.EASY)

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

class CustomType(var name: Int? = 0)

class CustomTypeConverter : TypeConverter<Int, CustomType>() {
    override fun getDBValue(model: CustomType?) = model?.name

    override fun getModelValue(data: Int?) = if (data == null) {
        null
    } else {
        CustomType(data)
    }

}

class CustomEnumTypeConverter : TypeConverter<String, Difficulty>() {
    override fun getDBValue(model: Difficulty?) = model?.name?.substring(0..0)

    override fun getModelValue(data: String?) = when (data) {
        "E" -> Difficulty.EASY
        "M" -> Difficulty.MEDIUM
        "H" -> Difficulty.HARD
        else -> Difficulty.HARD
    }

}

@com.raizlabs.dbflow5.annotation.TypeConverter
class BlobConverter : TypeConverter<Blob, ByteArray>() {

    override fun getDBValue(model: ByteArray?): Blob? {
        return if (model == null) null else Blob(model)
    }

    override fun getModelValue(data: Blob?): ByteArray? {
        return data?.blob
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
class NullableNumbers(@PrimaryKey var id: Int = 0,
                      @Column var f: Float? = null,
                      @Column var d: Double? = null,
                      @Column var l: Long? = null,
                      @Column var i: Int? = null,
                      @Column var bigDecimal: BigDecimal? = null,
                      @Column var bigInteger: BigInteger? = null)

@Table(database = TestDatabase::class)
class NonNullKotlinModel(@PrimaryKey var name: String = "",
                         @Column var date: Date = Date(),
                         @Column var numb: Int = 0)