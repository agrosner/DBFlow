package com.dbflow5.query.operations

import com.dbflow5.adapter.AdapterCompanion
import com.dbflow5.adapter.makeLazyDBRepresentable
import com.dbflow5.adapter.WritableDBRepresentable
import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.converter.TypeConverter
import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import kotlin.reflect.KClass
import kotlin.jvm.JvmName

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

@Suppress("UNUSED_PARAMETER")
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
        adapter = makeLazyDBRepresentable(table),
    )

@Suppress("UNUSED_PARAMETER")
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
        adapter = makeLazyDBRepresentable(table),
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
        adapter = makeLazyDBRepresentable(table),
    )

@Suppress("UNUSED_PARAMETER")
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
        adapter = makeLazyDBRepresentable(table),
    )

@InternalDBFlowApi
data class TypeConvertedPropertyImpl<ModelType, ValueType, Table : Any>(
        override val adapter: WritableDBRepresentable<Table>,
        override val valueConverter: TypeConverterValueConverter<ValueType, ModelType>,
        override val nameAlias: NameAlias,
        private val distinct: Boolean = false,
) : TypeConvertedProperty<ModelType, ValueType, Table>,
    DistinctTypeConvertedProperty<ModelType, Table>,
    AliasedProperty<ModelType, Table> {

    override val query: String = nameAlias.query
    @Suppress("UNCHECKED_CAST")
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