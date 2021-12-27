package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.annotation.Fts3
import com.dbflow5.annotation.Fts4
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.QueryModel
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.Unique
import com.dbflow5.annotation.UniqueGroup
import com.dbflow5.converter.TypeConverter
import com.dbflow5.data.Blob
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.query.SQLiteStatementListener
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

@Table(database = TestDatabase::class)
class SimpleQuickCheckModel(
    @PrimaryKey(
        quickCheckAutoIncrement = true,
        autoincrement = true
    ) var name: Int = 0
)

@Table(
    database = TestDatabase::class,
    insertConflict = ConflictAction.FAIL,
    updateConflict = ConflictAction.FAIL
)
class NumberModel(@PrimaryKey var id: Int = 0)

@Table(database = TestDatabase::class)
class CharModel(@PrimaryKey var id: Int = 0, @Column var exampleChar: Char? = null)

@Table(database = TestDatabase::class)
class TwoColumnModel(
    @PrimaryKey var name: String? = "",
    @Column(defaultValue = "56") var id: Int = 0
)

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
open class AllFieldsModel(
    @PrimaryKey var name: String? = null,
    var count: Int? = 0,
    @Column(getterName = "getTruth")
    var truth: Boolean = false,
    internal val finalName: String = "",
    @ColumnIgnore private val hidden: Int = 0
) {

    companion object {

        // example field to ensure static not used.
        var COUNT: Int = 0
    }
}

@Table(database = TestDatabase::class, allFields = true)
class SubclassAllFields(@Column var order: Int = 0) : AllFieldsModel()

@Table(database = TestDatabase::class, assignDefaultValuesFromCursor = false)
class DontAssignDefaultModel(
    @PrimaryKey var name: String? = null,
    @Column(getterName = "getNullableBool") var nullableBool: Boolean? = null,
    @Column var index: Int = 0
)

@Table(database = TestDatabase::class, orderedCursorLookUp = true)
class OrderCursorModel(
    @Column var age: Int = 0,
    @PrimaryKey var id: Int = 0,
    @Column var name: String? = ""
)

@Table(database = TestDatabase::class)
class TypeConverterModel(
    @PrimaryKey var id: Int = 0,
    @Column var opaqueData: ByteArray? = null,
    @Column var blob: Blob? = null,
    @Column(typeConverter = CustomTypeConverter::class)
    @PrimaryKey var customType: CustomType? = null
)

@Table(database = TestDatabase::class)
class EnumTypeConverterModel(
    @PrimaryKey var id: Int = 0,
    @Column var blob: Blob? = null,
    @Column var byteArray: ByteArray? = null,
    @Column(typeConverter = CustomEnumTypeConverter::class)
    var difficulty: Difficulty = Difficulty.EASY
)

@Table(database = TestDatabase::class, allFields = true)
class FeedEntry(
    @PrimaryKey var id: Int = 0,
    var title: String? = null,
    var subtitle: String? = null
)

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

data class CustomType(val name: Int = 0)

class CustomTypeConverter : TypeConverter<Int, CustomType>() {
    override fun getDBValue(model: CustomType) = model.name

    override fun getModelValue(data: Int) = CustomType(data)

}

class CustomEnumTypeConverter : TypeConverter<String, Difficulty>() {
    override fun getDBValue(model: Difficulty) = model.name.substring(0..0)

    override fun getModelValue(data: String) = when (data) {
        "E" -> Difficulty.EASY
        "M" -> Difficulty.MEDIUM
        "H" -> Difficulty.HARD
        else -> Difficulty.HARD
    }

}

@Table(database = TestDatabase::class)
class DefaultModel(
    @PrimaryKey @Column(defaultValue = "5") var id: Int? = 0,
    @Column(defaultValue = "5.0") var location: Double? = 0.0,
    @Column(defaultValue = "\"String\"") var name: String? = ""
)

@Table(database = TestDatabase::class)
class NullableNumbers(
    @PrimaryKey var id: Int = 0,
    @Column var f: Float? = null,
    @Column var d: Double? = null,
    @Column var l: Long? = null,
    @Column var i: Int? = null,
    @Column var bigDecimal: BigDecimal? = null,
    @Column var bigInteger: BigInteger? = null
)

@Table(database = TestDatabase::class)
class NonNullKotlinModel(
    @PrimaryKey var name: String = "",
    @Column var date: Date = Date(),
    @Column var numb: Int = 0
)

@Table(database = TestDatabase::class)
class Owner {
    @PrimaryKey(autoincrement = true)
    var id: Int = 0

    @Column
    var name: String? = null
}

@Table(database = TestDatabase::class)
class Dog {
    @ForeignKey(onDelete = ForeignKeyAction.CASCADE, stubbedRelationship = true)
    var owner: Owner? = null

    @PrimaryKey(autoincrement = true)
    var id: Int = 0

    @Column
    var name: String? = null
}

@Table(database = TestDatabase::class)
data class Currency(
    @PrimaryKey(autoincrement = true) var id: Long = 0,
    @Column @Unique var symbol: String? = null,
    @Column var shortName: String? = null,
    @Column @Unique var name: String = ""
) // nullability of fields are respected. We will not assign a null value to this field.

@JvmInline
value class Password(val value: String)

@JvmInline
value class Email(val value: String)

@Table(database = TestDatabase::class)
class UserInfo(
    @PrimaryKey
    @set:JvmName("setEmail")
    @get:JvmName("getEmail")
    var email: Email,
    @get:JvmName("getPassword")
    @set:JvmName("setPassword")
    var password: Password
) {
    constructor() : this(Email(""), Password(""))
}


@Table(database = TestDatabase::class)
class InternalClass internal constructor(
    @PrimaryKey
    @get:JvmName("getId")
    @set:JvmName("setId")
    internal var id: String = ""
)

@Table(database = TestDatabase::class, uniqueColumnGroups = [UniqueGroup(1)])
class UniqueModel(
    @PrimaryKey var id: String = "",
    @Unique(uniqueGroups = [1]) var name: String = "",
    @ForeignKey @Unique(uniqueGroups = [1]) var model: TypeConverterModel? = null
)

@Table(database = TestDatabase::class)
@Fts3
class Fts3Model(var name: String = "")

@Table(database = TestDatabase::class)
class Fts4Model(
    @PrimaryKey(autoincrement = true)
    var id: Int = 0,
    var name: String = ""
)

@Table(database = TestDatabase::class)
@Fts4(contentTable = Fts4Model::class)
class Fts4VirtualModel2(var name: String = "")
