package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.converter.TypeConverter
import com.raizlabs.android.dbflow.data.Blob

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