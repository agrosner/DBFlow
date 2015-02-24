package com.raizlabs.android.dbflow.processor.model.builder;

import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: Responsible for responsible writing of SQL wrapper classes.
 */
public class SqlQueryBuilder extends QueryBuilder<SqlQueryBuilder> {


    public SqlQueryBuilder() {
    }

    public SqlQueryBuilder(Object object) {
        super(object);
    }

    public SqlQueryBuilder appendSelect() {
        return append("new Select(projection)");
    }

    public SqlQueryBuilder appendUpdate() {
        return append("new Update()");
    }

    public SqlQueryBuilder appendDelete() {
        return append("new Delete()");
    }

    public SqlQueryBuilder appendGetDatabase(String databaseName) {
        return append(String.format("FlowManager.getDatabase(\"%1s\").getWritableDatabase()", databaseName));
    }

    public SqlQueryBuilder appendTable(String method, String databaseName, String tableName) {
        return append(String.format("\n.%1s(FlowManager.getTableClassForName(\"%1s\", \"%1s\"))", method, databaseName, tableName));
    }

    public SqlQueryBuilder appendFromTable(String databaseName, String tableName) {
        return appendTable("from", databaseName, tableName);
    }

    public SqlQueryBuilder appendTable(String databaseName, String tableName) {
        return appendTable("table", databaseName, tableName);
    }

    public SqlQueryBuilder appendUpdateConflictAction() {
        return append("\n.conflictAction(adapter.getUpdateOnConflictAction())");
    }

    public SqlQueryBuilder appendSet() {
        return append("\n.set().conditionValues(values)");
    }


    public SqlQueryBuilder appendWhere() {
        return append("\n.where(selection, selectionArgs)");
    }

    public SqlQueryBuilder appendPathSegments(ProcessorManager processorManager, String databaseName, String tableName, ContentUri.PathSegment[] pathSegments) {
        TableDefinition tableDefinition = processorManager.getTableDefinition(databaseName, tableName);
        if (tableDefinition == null) {
            processorManager.logError("Could not find table definition for %1s from %1s", tableName, databaseName);
        } else {
            for (ContentUri.PathSegment pathSegment : pathSegments) {
                ColumnDefinition columnDefinition = tableDefinition.mColumnMap.get(pathSegment.column());
                if (columnDefinition == null) {
                    processorManager.logError("Column %1s not found for table %1s", pathSegment.column(), tableDefinition.tableName);
                } else {
                    append(String.format("\n.and(Condition.column%s(\"%1s\").is(",
                            (columnDefinition.hasTypeConverter && !columnDefinition.isBoolean)
                                    ? "Raw" : "", pathSegment.column()));

                    // primitive use value of
                    if (columnDefinition.element.asType().getKind().isPrimitive()) {
                        String name = columnDefinition.element.asType().toString();

                        // handle char
                        if ("char" .equals(name)) {
                            name = "character";
                        }
                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        append(String.format("%1s.valueOf(uri.getPathSegments().get(%1d))", name, pathSegment.segment()));
                    } else {
                        append(String.format("uri.getPathSegments().get(%1d)", pathSegment.segment()));
                    }
                    append("))");
                }
            }
        }
        return this;
    }

    public SqlQueryBuilder appendInsertWithOnConflict(String tableName) {
        return append(String.format("\n.insertWithOnConflict(\"%1s\", null, values, " +
                "ConflictAction.getSQLiteDatabaseAlgorithmInt(adapter.getInsertOnConflictAction()))", tableName));
    }

    public SqlQueryBuilder appendQuery() {
        return append("\n.query()");
    }

    public SqlQueryBuilder appendCount() {
        return append("\n.count()");
    }

}
