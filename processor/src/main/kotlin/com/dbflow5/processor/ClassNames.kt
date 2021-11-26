package com.dbflow5.processor

import com.dbflow5.annotation.ConflictAction
import com.squareup.javapoet.ClassName

/**
 * Description: The static FQCN string file to assist in providing class names for imports and more in the Compiler
 */
object ClassNames {


    val BASE_PACKAGE = "com.dbflow5"
    val FLOW_MANAGER_PACKAGE = "$BASE_PACKAGE.config"
    val DATABASE_HOLDER_STATIC_CLASS_NAME = "GeneratedDatabaseHolder"
    val CONVERTER = "$BASE_PACKAGE.converter"
    val ADAPTER = "$BASE_PACKAGE.adapter"
    val QUERY_PACKAGE = "$BASE_PACKAGE.query"
    val STRUCTURE = "$BASE_PACKAGE.structure"
    val DATABASE = "$BASE_PACKAGE.database"
    val QUERIABLE = "$ADAPTER.queriable"
    val PROPERTY_PACKAGE = "$QUERY_PACKAGE.property"
    val CONFIG = "$BASE_PACKAGE.config"
    val SAVEABLE = "$ADAPTER.saveable"
    val PROVIDER = "$BASE_PACKAGE.provider"

    val DATABASE_HOLDER: ClassName = ClassName.get(CONFIG, "DatabaseHolder")
    val FLOW_MANAGER: ClassName = ClassName.get(CONFIG, "FlowManager")
    val BASE_DATABASE_DEFINITION_CLASSNAME: ClassName = ClassName.get(CONFIG, "DBFlowDatabase")

    val FLOW_CURSOR: ClassName = ClassName.get(DATABASE, "FlowCursor")
    val CONTENT_VALUES: ClassName = ClassName.get("android.content", "ContentValues")

    val MODEL_ADAPTER: ClassName = ClassName.get(ADAPTER, "ModelAdapter")
    val RETRIEVAL_ADAPTER: ClassName = ClassName.get(ADAPTER, "RetrievalAdapter")
    val MODEL: ClassName = ClassName.get(STRUCTURE, "Model")
    val MODEL_VIEW_ADAPTER: ClassName = ClassName.get(ADAPTER, "ModelViewAdapter")
    val OBJECT_TYPE: ClassName = ClassName.get(ADAPTER, "ObjectType")

    val DATABASE_STATEMENT: ClassName = ClassName.get(DATABASE, "DatabaseStatement")

    val QUERY: ClassName = ClassName.get(QUERY_PACKAGE, "Query")

    val TYPE_CONVERTER: ClassName = ClassName.get(CONVERTER, "TypeConverter")
    val TYPE_CONVERTER_GETTER: ClassName = ClassName.get(
        PROPERTY_PACKAGE,
        "TypeConvertedProperty.TypeConverterGetter"
    )

    val CONFLICT_ACTION: ClassName = ClassName.get(ConflictAction::class.java)

    val LOAD_FROM_CURSOR_LISTENER: ClassName =
        ClassName.get(QUERY_PACKAGE, "LoadFromCursorListener")
    val SQLITE_STATEMENT_LISTENER: ClassName =
        ClassName.get(QUERY_PACKAGE, "SQLiteStatementListener")


    val PROPERTY: ClassName = ClassName.get(PROPERTY_PACKAGE, "Property")
    val TYPE_CONVERTED_PROPERTY: ClassName =
        ClassName.get(PROPERTY_PACKAGE, "TypeConvertedProperty")
    val WRAPPER_PROPERTY: ClassName = ClassName.get(PROPERTY_PACKAGE, "WrapperProperty")

    val IPROPERTY: ClassName = ClassName.get(PROPERTY_PACKAGE, "IProperty")
    val INDEX_PROPERTY: ClassName = ClassName.get(PROPERTY_PACKAGE, "IndexProperty")
    val OPERATOR_GROUP: ClassName = ClassName.get(QUERY_PACKAGE, "OperatorGroup")

    val ICONDITIONAL: ClassName = ClassName.get(QUERY_PACKAGE, "IConditional")

    val BASE_MODEL: ClassName = ClassName.get(STRUCTURE, "BaseModel")
    val SIMPLE_MAP_CACHE: ClassName = ClassName.get("$QUERY_PACKAGE.cache", "SimpleMapCache")

    val CACHEABLE_MODEL_LOADER: ClassName = ClassName.get(QUERIABLE, "CacheableModelLoader")
    val SINGLE_MODEL_LOADER: ClassName = ClassName.get(QUERIABLE, "SingleModelLoader")
    val CACHEABLE_LIST_MODEL_LOADER: ClassName =
        ClassName.get(QUERIABLE, "CacheableListModelLoader")
    val LIST_MODEL_LOADER: ClassName = ClassName.get(QUERIABLE, "ListModelLoader")
    val CACHE_ADAPTER: ClassName = ClassName.get(ADAPTER, "CacheAdapter")

    val DATABASE_WRAPPER: ClassName = ClassName.get(DATABASE, "DatabaseWrapper")

    val SQLITE: ClassName = ClassName.get(QUERY_PACKAGE, "SQLite")

    val CACHEABLE_LIST_MODEL_SAVER: ClassName = ClassName.get(SAVEABLE, "CacheableListModelSaver")

    val SINGLE_KEY_CACHEABLE_MODEL_LOADER: ClassName =
        ClassName.get(QUERIABLE, "SingleKeyCacheableModelLoader")
    val SINGLE_KEY_CACHEABLE_LIST_MODEL_LOADER: ClassName =
        ClassName.get(QUERIABLE, "SingleKeyCacheableListModelLoader")

    val NON_NULL: ClassName = ClassName.get("android.support.annotation", "NonNull")
    val NON_NULL_X: ClassName = ClassName.get("androidx.annotation", "NonNull")

    val GENERATED: ClassName = ClassName.get("javax.annotation", "Generated")

    val STRING_UTILS: ClassName = ClassName.get(BASE_PACKAGE, "StringUtils")
}
