package com.raizlabs.android.dbflow.processor;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.squareup.javapoet.ClassName;

/**
 * Description: The static FQCN string file to assist in providing class names for imports and more in the Compiler
 */
public class ClassNames {


    public static final String BASE_PACKAGE = "com.raizlabs.android.dbflow";
    public static final String FLOW_MANAGER_PACKAGE = BASE_PACKAGE + ".config";
    public static final String DATABASE_HOLDER_STATIC_CLASS_NAME = "GeneratedDatabaseHolder";
    public static final String CONVERTER = BASE_PACKAGE + ".converter";
    public static final String STRUCTURE = BASE_PACKAGE + ".structure";
    public static final String CONTAINER = STRUCTURE + ".container";
    public static final String SQL = BASE_PACKAGE + ".sql";
    public static final String LANGUAGE = SQL + ".language";
    public static final String PROPERTY_PACKAGE = LANGUAGE + ".property";
    public static final String CONFIG = BASE_PACKAGE + ".config";
    public static final String BUILDER = SQL + ".builder";
    public static final String MIGRATION_PACKAGE = SQL + ".migration";
    public static final String LISTENER = STRUCTURE + ".listener";
    public static final String RUNTIME = BASE_PACKAGE + ".runtime";
    public static final String TRANSACTION = RUNTIME + ".transaction";
    public static final String PROCESS = TRANSACTION + ".process";

    public static final ClassName DATABASE_HOLDER = ClassName.get(CONFIG, "DatabaseHolder");
    public static final ClassName FLOW_SQLITE_OPEN_HELPER = ClassName.get(CONFIG, "FlowSQLiteOpenHelper");
    public static final ClassName FLOW_MANAGER = ClassName.get(CONFIG, "FlowManager");
    public static final ClassName BASE_DATABASE_DEFINITION_CLASSNAME = ClassName.get(CONFIG, "BaseDatabaseDefinition");

    public static final ClassName SQLITE_STATEMENT = ClassName.get("android.database.sqlite", "SQLiteStatement");
    public static final ClassName URI = ClassName.get("android.net", "Uri");
    public static final ClassName URI_MATCHER = ClassName.get("android.content", "UriMatcher");
    public static final ClassName CURSOR = ClassName.get("android.database", "Cursor");
    public static final ClassName CONTENT_VALUES = ClassName.get("android.content", "ContentValues");
    public static final ClassName CONTENT_URIS = ClassName.get("android.content", "ContentUris");

    public static final ClassName MODEL_ADAPTER = ClassName.get(STRUCTURE, "ModelAdapter");
    public static final ClassName QUERY_MODEL_ADAPTER = ClassName.get(STRUCTURE, "QueryModelAdapter");
    public static final ClassName MODEL = ClassName.get(STRUCTURE, "Model");
    public static final ClassName MODEL_VIEW_ADAPTER = ClassName.get(STRUCTURE, "ModelViewAdapter");
    public static final ClassName MODEL_VIEW = ClassName.get(STRUCTURE, "BaseModelView");

    public static final ClassName CONDITION_QUERY_BUILDER = ClassName.get(BUILDER, "ConditionQueryBuilder");
    public static final ClassName CONDITION = ClassName.get(BUILDER, "Condition");

    public static final ClassName SQL_UTILS = ClassName.get(SQL, "SqlUtils");
    public static final ClassName QUERY = ClassName.get(SQL, "Query");

    public static final ClassName TYPE_CONVERTER = ClassName.get(CONVERTER, "TypeConverter");
    public static final ClassName PROCESS_MODEL_INFO = ClassName.get(PROCESS, "ProcessModelInfo");

    public static final ClassName FLOW_MANAGER_STATIC_INTERFACE = ClassName.get(FLOW_MANAGER_PACKAGE, "DatabaseHolder");

    public static final ClassName MIGRATION = ClassName.get(MIGRATION_PACKAGE, "Migration");

    public static final ClassName CONFLICT_ACTION = ClassName.get(ConflictAction.class);

    public static final ClassName CONTENT_VALUES_LISTENER = ClassName.get(LISTENER, "ContentValuesListener");
    public static final ClassName LOAD_FROM_CURSOR_LISTENER = ClassName.get(LISTENER, "LoadFromCursorListener");
    public static final ClassName SQLITE_STATEMENT_LISTENER = ClassName.get(LISTENER, "SQLiteStatementListener");


    public static final ClassName DELETE_MODEL_LIST_TRANSACTION = ClassName.get(PROCESS, "DeleteModelListTransaction");
    public static final ClassName SAVE_MODEL_LIST_TRANSACTION = ClassName.get(PROCESS, "SaveModelTransaction");
    public static final ClassName UPDATE_MODEL_LIST_TRANSACTION = ClassName.get(PROCESS, "UpdateModelListTransaction");
    public static final ClassName INSERT_MODEL_LIST_TRANSACTION = ClassName.get(PROCESS, "InsertModelTransaction");

    public static final ClassName PROPERTY = ClassName.get(PROPERTY_PACKAGE, "Property");
    public static final ClassName IPROPERTY = ClassName.get(PROPERTY_PACKAGE, "IProperty");
    public static final ClassName BASE_PROPERTY = ClassName.get(PROPERTY_PACKAGE, "BaseProperty");
    public static final ClassName INDEX_PROPERTY = ClassName.get(PROPERTY_PACKAGE, "IndexProperty");
    public static final ClassName CONDITION_GROUP = ClassName.get(LANGUAGE, "ConditionGroup");
    public static final ClassName SELECT = ClassName.get(LANGUAGE, "Select");
    public static final ClassName UPDATE = ClassName.get(LANGUAGE, "Update");
    public static final ClassName DELETE = ClassName.get(LANGUAGE, "Delete");
    public static final ClassName METHOD = ClassName.get(LANGUAGE, "Method");

    public static final ClassName BASE_CONTENT_PROVIDER = ClassName.get(RUNTIME, "BaseContentProvider");
    public static final ClassName PROPERTY_CONVERTER = ClassName.get(RUNTIME + ".BaseContentProvider", "PropertyConverter");

    public static final ClassName MODEL_CONTAINER_UTILS = ClassName.get(CONTAINER, "ModelContainerUtils");
    public static final ClassName MODEL_CONTAINER = ClassName.get(CONTAINER, "ModelContainer");
    public static final ClassName MODEL_CONTAINER_ADAPTER = ClassName.get(CONTAINER, "ModelContainerAdapter");
    public static final ClassName FOREIGN_KEY_CONTAINER = ClassName.get(CONTAINER,  "ForeignKeyContainer");
    public static final ClassName BASE_MODEL = ClassName.get(STRUCTURE, "BaseModel");
}
