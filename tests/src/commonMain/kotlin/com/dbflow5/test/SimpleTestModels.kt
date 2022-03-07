package com.dbflow5.test

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Query
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.Unique
import com.dbflow5.annotation.UniqueGroup
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.query.DatabaseStatementListener
import kotlin.jvm.JvmName

@Query
data class SimpleCustomModel(@Column val name: String?)

@Table
data class CharModel(
    @PrimaryKey val id: Int,
    val exampleChar: Char?,
)

@Table
data class EnumModel(
    @PrimaryKey val id: Int = 0,
    val difficulty: Difficulty? = Difficulty.EASY,
)

@Table
open class AllFieldsModel(
    @PrimaryKey var name: String? = null,
    var count: Int? = 0,
    @Column
    var truth: Boolean = false,
    internal val finalName: String = "",
    @ColumnIgnore private val hidden: Int = 0
) {

    companion object {

        // example field to ensure static not used.
        var COUNT: Int = 0
    }
}

@Table
class SubclassAllFields(@PrimaryKey var order: Int = 0) : AllFieldsModel()

@Table(assignDefaultValuesFromCursor = false)
class DontAssignDefaultModel(
    @PrimaryKey var name: String? = null,
    @Column var nullableBool: Boolean? = null,
    @Column var index: Int = 0
)

@Table
class FeedEntry(
    @PrimaryKey var id: Int = 0,
    var title: String? = null,
    var subtitle: String? = null
)

/*
@Table
@ManyToMany(
    generatedTableClassName = "Refund", referencedTable = Transfer::class,
    referencedTableColumnName = "refund_in", thisTableColumnName = "refund_out",
    saveForeignKeyModels = true
)
data class Transfer(@PrimaryKey var transfer_id: UUID = UUID.randomUUID())

@Table
data class Transfer2(
    @PrimaryKey
    var id: UUID = UUID.randomUUID(),
    @ForeignKey
    var origin: Account? = null
)

@Table
data class Account(@PrimaryKey var id: UUID = UUID.randomUUID())
*/

@Table
class SqlListenerModel(@PrimaryKey var id: Int = 0) : DatabaseStatementListener {
    override fun onBind(
        type: DatabaseStatementListener.Type,
        databaseStatement: DatabaseStatement
    ) = Unit
}

@Table
class DefaultModel(
    @PrimaryKey @Column(defaultValue = "5") var id: Int? = 0,
    @Column(defaultValue = "5.0") var location: Double? = 0.0,
    @Column(defaultValue = "\"String\"") var name: String? = ""
)

@Table
class NullableNumbers(
    @PrimaryKey var id: Int = 0,
    @Column var f: Float? = null,
    @Column var d: Double? = null,
    @Column var l: Long? = null,
    @Column var i: Int? = null,
    //@Column var bigDecimal: BigDecimal? = null,
    //@Column var bigInteger: BigInteger? = null
)

@Table
data class NonNullKotlinModel(
    @PrimaryKey val name: String = "",
    @Column val date: Long = 0L,
    @Column val numb: Int = 0
)

@Table
class Owner {
    @PrimaryKey(autoincrement = true)
    var id: Int = 0

    @Column
    var name: String? = null
}

@Table
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

@Table
data class UserInfo(
    @PrimaryKey
    @get:JvmName("getEmail") val email: Email,
    @get:JvmName("getPassword") val password: Password
)

@Table
class InternalClass internal constructor(
    @PrimaryKey
    @get:JvmName("getId")
    @set:JvmName("setId")
    internal var id: String = ""
)

@Table(uniqueColumnGroups = [UniqueGroup(1)])
class UniqueModel(
    @PrimaryKey var id: String = "",
    @Unique(uniqueGroups = [1]) var name: String = "",
    @ForeignKey @Unique(uniqueGroups = [1]) var model: TypeConverterModel? = null
)
