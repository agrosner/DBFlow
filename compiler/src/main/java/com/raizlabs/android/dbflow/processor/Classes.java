package com.raizlabs.android.dbflow.processor;

/**
 * Description: The static FQCN string file to assist in providing class names for imports and more in the Compiler
 */
public class Classes {

    public static final String CURSOR = "android.database.Cursor";

    public static final String CONTENT_VALUES = "android.content.ContentValues";

    public static final String BASE_PACKAGE = "com.raizlabs.android.dbflow.";

    public static final String STRUCTURE = BASE_PACKAGE + "structure.";

    public static final String CONTAINER = STRUCTURE + "container.";

    public static final String SQL = BASE_PACKAGE + "sql.";

    public static final String CONFIG = BASE_PACKAGE + "config.";

    public static final String BUILDER = SQL + "builder.";

    public static final String MODEL_ADAPTER = STRUCTURE + "ModelAdapter";

    public static final String MODEL_CONTAINER = CONTAINER + "ModelContainer";

    public static final String MODEL = STRUCTURE + "Model";

    public static final String CONDITION_QUERY_BUILDER = BUILDER + "ConditionQueryBuilder";

    public static final String SELECT = SQL + "language.Select";

    public static final String FLOW_MANAGER = CONFIG + "FlowManager";

    public static final String SQL_UTILS = SQL + "SqlUtils";

    public static final String CONDITION = BUILDER + "Condition";

    public static final String TYPE_CONVERTER = "com.raizlabs.android.dbflow.converter.TypeConverter";

    public static final String MODEL_VIEW = STRUCTURE + "BaseModelView";

    public static final String DELETE = SQL + "language.Delete";

    public static final String CONTAINER_ADAPTER = CONTAINER + "ContainerAdapter";

    public static final String MODEL_CONTAINER_UTILS = CONTAINER + "ModelContainerUtils";

    public static final String TRANSACTION_MANAGER = BASE_PACKAGE + "runtime.TransactionManager";

    public static final String PROCESS_MODEL_INFO = BASE_PACKAGE + "runtime.transaction.process.ProcessModelInfo";

    public static final String DBTRANSACTION_INFO = BASE_PACKAGE + "runtime.DBTransactionInfo";

    public static final String FLOW_MANAGER_PACKAGE = "com.raizlabs.android.dbflow.config";

    public static final String DATABASE_HOLDER_STATIC_CLASS_NAME = "Database$Holder";

    public static final String BASE_DATABASE_DEFINITION = "BaseDatabaseDefinition";

    public static final String MAP = "java.util.Map";

    public static final String HASH_MAP = "java.util.HashMap";

    public static final String LIST = "java.util.List";

    public static final String ARRAY_LIST = "java.util.ArrayList";

    public static final String FLOW_MANAGER_STATIC_INTERFACE = FLOW_MANAGER_PACKAGE + ".DatabaseHolder";

    public static final String MODEL_VIEW_ADAPTER = STRUCTURE + "ModelViewAdapter";

    public static final String MIGRATION = SQL + "migration.Migration";

    public static final String SQLITE_STATEMENT = "android.database.sqlite.SQLiteStatement";
}
