package com.raizlabs.dbflow5.processor

import com.raizlabs.dbflow5.annotation.ConflictAction
import com.squareup.javapoet.ClassName

/**
 * Description: The static FQCN string file to assist in providing class names for imports and more in the Compiler
 */
object ClassNames {


    val BASE_PACKAGE = "com.raizlabs.dbflow5"
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
    val RUNTIME = "$BASE_PACKAGE.runtime"
    val SAVEABLE = "$ADAPTER.saveable"
    val PROVIDER = "$BASE_PACKAGE.provider"

    val DATABASE_HOLDER = ClassName.get(CONFIG, "DatabaseHolder")
    val FLOW_MANAGER = ClassName.get(CONFIG, "FlowManager")
    val BASE_DATABASE_DEFINITION_CLASSNAME = ClassName.get(CONFIG, "DBFlowDatabase")

    val URI = ClassName.get("android.net", "Uri")
    val URI_MATCHER = ClassName.get("android.content", "UriMatcher")
    val CURSOR = ClassName.get("android.database", "Cursor")
    val FLOW_CURSOR = ClassName.get(DATABASE, "FlowCursor")
    val DATABASE_UTILS = ClassName.get("android.database", "DatabaseUtils")
    val CONTENT_VALUES = ClassName.get("android.content", "ContentValues")
    val CONTENT_URIS = ClassName.get("android.content", "ContentUris")

    val MODEL_ADAPTER = ClassName.get(ADAPTER, "ModelAdapter")
    val QUERY_MODEL_ADAPTER = ClassName.get(ADAPTER, "QueryModelAdapter")
    val MODEL = ClassName.get(STRUCTURE, "Model")
    val MODEL_VIEW_ADAPTER = ClassName.get(ADAPTER, "ModelViewAdapter")

    val DATABASE_STATEMENT = ClassName.get(DATABASE, "DatabaseStatement")

    val QUERY = ClassName.get(QUERY_PACKAGE, "Query")

    val TYPE_CONVERTER = ClassName.get(CONVERTER, "TypeConverter")
    val TYPE_CONVERTER_GETTER: ClassName = ClassName.get(PROPERTY_PACKAGE,
        "TypeConvertedProperty.TypeConverterGetter")

    val CONFLICT_ACTION = ClassName.get(ConflictAction::class.java)

    val CONTENT_VALUES_LISTENER = ClassName.get(QUERY_PACKAGE, "ContentValuesListener")
    val LOAD_FROM_CURSOR_LISTENER = ClassName.get(QUERY_PACKAGE, "LoadFromCursorListener")
    val SQLITE_STATEMENT_LISTENER = ClassName.get(QUERY_PACKAGE, "SQLiteStatementListener")


    val PROPERTY = ClassName.get(PROPERTY_PACKAGE, "Property")
    val TYPE_CONVERTED_PROPERTY = ClassName.get(PROPERTY_PACKAGE, "TypeConvertedProperty")
    val WRAPPER_PROPERTY = ClassName.get(PROPERTY_PACKAGE, "WrapperProperty")

    val IPROPERTY = ClassName.get(PROPERTY_PACKAGE, "IProperty")
    val INDEX_PROPERTY = ClassName.get(PROPERTY_PACKAGE, "IndexProperty")
    val OPERATOR_GROUP = ClassName.get(QUERY_PACKAGE, "OperatorGroup")

    val ICONDITIONAL = ClassName.get(QUERY_PACKAGE, "IConditional")

    val BASE_CONTENT_PROVIDER = ClassName.get(PROVIDER, "BaseContentProvider")

    val BASE_MODEL = ClassName.get(STRUCTURE, "BaseModel")
    val MODEL_CACHE = ClassName.get("$QUERY_PACKAGE.cache", "ModelCache")
    val MULTI_KEY_CACHE_CONVERTER = ClassName.get("$QUERY_PACKAGE.cache", "IMultiKeyCacheConverter")

    val CACHEABLE_MODEL_LOADER = ClassName.get(QUERIABLE, "CacheableModelLoader")
    val SINGLE_MODEL_LOADER = ClassName.get(QUERIABLE, "SingleModelLoader")
    val CACHEABLE_LIST_MODEL_LOADER = ClassName.get(QUERIABLE, "CacheableListModelLoader")
    val LIST_MODEL_LOADER = ClassName.get(QUERIABLE, "ListModelLoader")
    val CACHE_ADAPTER = ClassName.get(ADAPTER, "CacheAdapter")

    val DATABASE_WRAPPER = ClassName.get(DATABASE, "DatabaseWrapper")

    val SQLITE = ClassName.get(QUERY_PACKAGE, "SQLite")

    val CACHEABLE_LIST_MODEL_SAVER = ClassName.get(SAVEABLE, "CacheableListModelSaver")
    val SINGLE_MODEL_SAVER = ClassName.get(SAVEABLE, "ModelSaver")
    val AUTOINCREMENT_MODEL_SAVER = ClassName.get(SAVEABLE, "AutoIncrementModelSaver")

    val SINGLE_KEY_CACHEABLE_MODEL_LOADER = ClassName.get(QUERIABLE, "SingleKeyCacheableModelLoader")
    val SINGLE_KEY_CACHEABLE_LIST_MODEL_LOADER = ClassName.get(QUERIABLE, "SingleKeyCacheableListModelLoader")

    val NON_NULL = ClassName.get("android.support.annotation", "NonNull")

    val GENERATED = ClassName.get("javax.annotation", "Generated")

    val STRING_UTILS = ClassName.get(BASE_PACKAGE, "StringUtils")
}
