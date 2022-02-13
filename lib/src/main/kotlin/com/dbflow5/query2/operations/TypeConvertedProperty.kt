package com.dbflow5.query2.operations

import com.dbflow5.adapter.AdapterCompanion
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.adapter.makeLazySQLObjectAdapter
import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.converter.TypeConverter
import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import kotlin.reflect.KClass

/**
 * Description: A special [Property] with a type converter.
 */
interface TypeConvertedProperty<ModelType, ValueType, Table : Any> :
    PropertyStart<ModelType, Table> {

    val dataProperty: Property<ValueType, Table>

    fun <ValueType : Any, ModelType : Any> typeConverter(): TypeConverter<ValueType, ModelType>

    override fun withTable(tableName: String): TypeConvertedProperty<ModelType, ValueType, Table>
}

interface DistinctTypeConvertedProperty<ValueType, Table : Any> :
    DistinctProperty<ValueType, Table>

inline fun <Table : Any, reified ValueType : Any, ModelType : Any> AdapterCompanion<Table>.typeConvertedProperty(
    unusedData: KClass<ValueType>,
    unusedModel: KClass<ModelType>,
    columnName: String,
    noinline typeConverterGetter: () -> TypeConverter<ValueType, ModelType>
): TypeConvertedProperty<ModelType, ValueType, Table> =
    TypeConvertedPropertyImpl(
        valueConverter = typeConverterValueConverter(
            inferValueConverter(),
            typeConverterGetter,
        ),
        nameAlias = columnName.nameAlias,
        adapter = makeLazySQLObjectAdapter(table),
    )

@JvmName("nullableDataTypeConvertedProperty")
inline fun <Table : Any, reified ValueType : Any, ModelType : Any> AdapterCompanion<Table>.typeConvertedProperty(
    unusedModel: KClass<ModelType>,
    columnName: String,
    noinline typeConverterGetter: () -> TypeConverter<ValueType, ModelType>
): TypeConvertedProperty<ModelType, ValueType?, Table> =
    TypeConvertedPropertyImpl(
        valueConverter = typeConverterValueConverter(
            inferValueConverter(),
            typeConverterGetter,
        ),
        nameAlias = columnName.nameAlias,
        adapter = makeLazySQLObjectAdapter(table),
    )

inline fun <Table : Any, reified ValueType : Any, ModelType : Any> AdapterCompanion<Table>.typeConvertedProperty(
    columnName: String,
    noinline typeConverterGetter: () -> TypeConverter<ValueType, ModelType>
): TypeConvertedProperty<ModelType?, ValueType?, Table> =
    TypeConvertedPropertyImpl(
        valueConverter = typeConverterValueConverter(
            inferValueConverter(),
            typeConverterGetter,
        ),
        nameAlias = columnName.nameAlias,
        adapter = makeLazySQLObjectAdapter(table),
    )

@JvmName("nullableModelTypeConvertedProperty")
inline fun <Table : Any, reified ValueType : Any, ModelType : Any> AdapterCompanion<Table>.typeConvertedProperty(
    unusedModel: KClass<ValueType>,
    columnName: String,
    noinline typeConverterGetter: () -> TypeConverter<ValueType, ModelType>
): TypeConvertedProperty<ModelType?, ValueType, Table> =
    TypeConvertedPropertyImpl(
        valueConverter = typeConverterValueConverter(
            inferValueConverter(),
            typeConverterGetter,
        ),
        nameAlias = columnName.nameAlias,
        adapter = makeLazySQLObjectAdapter(table),
    )

@InternalDBFlowApi
data class TypeConvertedPropertyImpl<ModelType, ValueType, Table : Any>(
    override val adapter: SQLObjectAdapter<Table>,
    override val valueConverter: TypeConverterValueConverter<ValueType, ModelType>,
    override val nameAlias: NameAlias,
    private val distinct: Boolean = false,
) : TypeConvertedProperty<ModelType, ValueType, Table>,
    DistinctTypeConvertedProperty<ModelType, Table>,
    AliasedProperty<ModelType, Table> {

    override val query: String = nameAlias.query
    override fun <ValueType : Any, ModelType : Any> typeConverter(): TypeConverter<ValueType, ModelType> =
        valueConverter.typeConverter as TypeConverter<ValueType, ModelType>

    override fun withTable(tableName: String): TypeConvertedProperty<ModelType, ValueType, Table> =
        copy(
            nameAlias = nameAlias.newBuilder()
                .withTable(tableName)
                .build()
        )

    override fun distinct(): DistinctProperty<ModelType, Table> =
        copy(
            distinct = true,
        )

    override val dataProperty: Property<ValueType, Table> = PropertyImpl(
        adapter = adapter,
        nameAlias = nameAlias,
        valueConverter = valueConverter.innerValueConverter,
        distinct = distinct,
    )

    override fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean
    ): AliasedProperty<ModelType, Table> =
        copy(
            nameAlias = nameAlias.newBuilder()
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
                .`as`(name)
                .build(),
        )
}