package com.raizlabs.android.dbflow.processor

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.squareup.javapoet.ClassName

/**
 * Description: The static FQCN string file to assist in providing class names for imports and more in the Compiler
 */
object ClassNames {


    val BASE_PACKAGE = "com.raizlabs.android.dbflow"
    val FLOW_MANAGER_PACKAGE = BASE_PACKAGE + ".config"
    val DATABASE_HOLDER_STATIC_CLASS_NAME = "GeneratedDatabaseHolder"
    val CONVERTER = BASE_PACKAGE + ".converter"
    val STRUCTURE = BASE_PACKAGE + ".structure"
    val DATABASE = STRUCTURE + ".database"
    val SQL = BASE_PACKAGE + ".sql"
    val LANGUAGE = SQL + ".language"
    val QUERIABLE = SQL + ".queriable"
    val PROPERTY_PACKAGE = LANGUAGE + ".property"
    val CONFIG = BASE_PACKAGE + ".config"
    val MIGRATION_PACKAGE = SQL + ".migration"
    val LISTENER = STRUCTURE + ".listener"
    val RUNTIME = BASE_PACKAGE + ".runtime"
    val TRANSACTION = RUNTIME + ".transaction"
    val SAVEABLE = SQL + ".saveable"

    val DATABASE_HOLDER = ClassName.get(CONFIG, "DatabaseHolder")
    val FLOW_MANAGER = ClassName.get(CONFIG, "FlowManager")
    val BASE_DATABASE_DEFINITION_CLASSNAME = ClassName.get(CONFIG, "DatabaseDefinition")

    val URI = ClassName.get("android.net", "Uri")
    val URI_MATCHER = ClassName.get("android.content", "UriMatcher")
    val CURSOR = ClassName.get("android.database", "Cursor")
    val FLOW_CURSOR = ClassName.get(DATABASE, "FlowCursor")
    val DATABASE_UTILS = ClassName.get("android.database", "DatabaseUtils")
    val CONTENT_VALUES = ClassName.get("android.content", "ContentValues")
    val CONTENT_URIS = ClassName.get("android.content", "ContentUris")

    val MODEL_ADAPTER = ClassName.get(STRUCTURE, "ModelAdapter")
    val QUERY_MODEL_ADAPTER = ClassName.get(STRUCTURE, "QueryModelAdapter")
    val MODEL = ClassName.get(STRUCTURE, "Model")
    val MODEL_VIEW_ADAPTER = ClassName.get(STRUCTURE, "ModelViewAdapter")
    val MODEL_VIEW = ClassName.get(STRUCTURE, "BaseModelView")

    val DATABASE_STATEMENT = ClassName.get(DATABASE, "DatabaseStatement")

    val QUERY = ClassName.get(SQL, "Query")

    val TYPE_CONVERTER = ClassName.get(CONVERTER, "TypeConverter")
    val TYPE_CONVERTER_GETTER: ClassName = ClassName.get(PROPERTY_PACKAGE,
            "TypeConvertedProperty.TypeConverterGetter")

    val MIGRATION = ClassName.get(MIGRATION_PACKAGE, "Migration")

    val CONFLICT_ACTION = ClassName.get(ConflictAction::class.java)

    val CONTENT_VALUES_LISTENER = ClassName.get(LISTENER, "ContentValuesListener")
    val LOAD_FROM_CURSOR_LISTENER = ClassName.get(LISTENER, "LoadFromCursorListener")
    val SQLITE_STATEMENT_LISTENER = ClassName.get(LISTENER, "SQLiteStatementListener")


    val PROPERTY = ClassName.get(PROPERTY_PACKAGE, "Property")
    val TYPE_CONVERTED_PROPERTY = ClassName.get(PROPERTY_PACKAGE, "TypeConvertedProperty")
    val WRAPPER_PROPERTY = ClassName.get(PROPERTY_PACKAGE, "WrapperProperty")

    val IPROPERTY = ClassName.get(PROPERTY_PACKAGE, "IProperty")
    val INDEX_PROPERTY = ClassName.get(PROPERTY_PACKAGE, "IndexProperty")
    val OPERATOR_GROUP = ClassName.get(LANGUAGE, "OperatorGroup")

    val ICONDITIONAL = ClassName.get(LANGUAGE, "IConditional")

    val BASE_CONTENT_PROVIDER = ClassName.get(RUNTIME, "BaseContentProvider")

    val BASE_MODEL = ClassName.get(STRUCTURE, "BaseModel")
    val MODEL_CACHE = ClassName.get(STRUCTURE + ".cache", "ModelCache")
    val MULTI_KEY_CACHE_CONVERTER = ClassName.get(STRUCTURE + ".cache", "IMultiKeyCacheConverter")

    val CACHEABLE_MODEL_LOADER = ClassName.get(QUERIABLE, "CacheableModelLoader")
    val SINGLE_MODEL_LOADER = ClassName.get(QUERIABLE, "SingleModelLoader")
    val CACHEABLE_LIST_MODEL_LOADER = ClassName.get(QUERIABLE, "CacheableListModelLoader")
    val LIST_MODEL_LOADER = ClassName.get(QUERIABLE, "ListModelLoader")

    val DATABASE_WRAPPER = ClassName.get(DATABASE, "DatabaseWrapper")

    val SQLITE = ClassName.get(LANGUAGE, "SQLite")

    val CACHEABLE_LIST_MODEL_SAVER = ClassName.get(SAVEABLE, "CacheableListModelSaver")

    val SINGLE_KEY_CACHEABLE_MODEL_LOADER = ClassName.get(QUERIABLE, "SingleKeyCacheableModelLoader")
    val SINGLE_KEY_CACHEABLE_LIST_MODEL_LOADER = ClassName.get(QUERIABLE, "SingleKeyCacheableListModelLoader")
}
