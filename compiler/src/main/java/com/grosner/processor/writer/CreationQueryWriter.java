package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.dbflow.sql.SQLiteType;
import com.grosner.processor.Classes;
import com.grosner.processor.ProcessorUtils;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.PrimaryKeyNotFoundException;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.model.ReflectionUtils;
import com.grosner.processor.model.builder.TableCreationQueryBuilder;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class CreationQueryWriter implements FlowWriter{

    private final TableDefinition tableDefinition;
    private final ProcessorManager manager;

    public CreationQueryWriter(ProcessorManager manager, TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
        this.manager = manager;
    }


    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                TableCreationQueryBuilder tableCreationQuery = new TableCreationQueryBuilder();
                tableCreationQuery.appendCreateTableIfNotExists(tableDefinition.tableName);

                ArrayList<QueryBuilder> mColumnDefinitions = new ArrayList<QueryBuilder>();
                for(ColumnDefinition columnDefinition: tableDefinition.columnDefinitions) {

                    TableCreationQueryBuilder queryBuilder = new TableCreationQueryBuilder();
                    if(columnDefinition.columnType == Column.FOREIGN_KEY) {
                        queryBuilder.appendForeignKeys(columnDefinition.foreignKeyReferences);
                    }


                    if (SQLiteType.containsClass(columnDefinition.columnFieldType)) {
                        queryBuilder.append(columnDefinition.columnName)
                                .appendSpace()
                                .appendType(columnDefinition.columnFieldType);
                    } else if (ReflectionUtils.isSubclassOf(columnDefinition.columnFieldType, Enum.class)) {
                        queryBuilder.append(columnDefinition.columnName)
                                .appendSpace()
                                .appendSQLiteType(SQLiteType.TEXT);
                    } else {
                        // TODO: move type converters into Annotations
                        /*TypeConverter typeConverter = FlowManager.getTypeConverterForClass(type);
                        if (typeConverter != null) {
                            tableCreationQuery.append(columnName)
                                    .appendSpace()
                                    .appendType(typeConverter.getDatabaseType());
                        }*/
                    }

                    mColumnDefinitions.add(queryBuilder.appendColumn(columnDefinition.column));
                }

                boolean isModelView = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                        tableDefinition.packageName +"." + tableDefinition.modelClassName,
                        manager.getElements().getTypeElement(Classes.MODEL_VIEW));

                // Views do not have primary keys
                if(!isModelView) {
                    if(tableDefinition.primaryColumnDefinitions.isEmpty()) {
                        throw new PrimaryKeyNotFoundException("Table: " + tableDefinition.tableName + " must define a primary key");
                    }

                    QueryBuilder primaryKeyQueryBuilder = new QueryBuilder().append("PRIMARY KEY(");
                    int count = 0;
                    int index = 0;
                    for (ColumnDefinition field : tableDefinition.primaryColumnDefinitions) {
                        if (field.columnType == Column.PRIMARY_KEY) {
                            count++;
                            primaryKeyQueryBuilder.append(field.columnName);
                            if (index < tableDefinition.primaryColumnDefinitions.size() - 1) {
                                primaryKeyQueryBuilder.append(", ");
                            }
                        }
                        index++;
                    }

                    if (count > 0) {
                        primaryKeyQueryBuilder.append(")");
                        mColumnDefinitions.add(primaryKeyQueryBuilder);
                    }

                    QueryBuilder foreignKeyQueryBuilder;
                    for (ColumnDefinition foreignKeyField : tableDefinition.foreignKeyDefinitions) {
                        foreignKeyQueryBuilder = new QueryBuilder().append("FOREIGN KEY(");

                        String[] foreignColumns = new String[foreignKeyField.foreignKeyReferences.length];
                        for (int i = 0; i < foreignColumns.length; i++) {
                            foreignColumns[i] = foreignKeyField.foreignKeyReferences[i].foreignColumnName();
                        }

                        String[] columns = new String[foreignKeyField.foreignKeyReferences.length];
                        for (int i = 0; i < columns.length; i++) {
                            columns[i] = foreignKeyField.foreignKeyReferences[i].columnName();
                        }

                        foreignKeyQueryBuilder.appendArray(columns)
                                .append(")").appendSpaceSeparated("REFERENCES")
                                .append(tableDefinition.tableName)
                                .append("(").appendArray(foreignColumns).append(")");

                        mColumnDefinitions.add(foreignKeyQueryBuilder);
                    }

                } else if (!tableDefinition.primaryColumnDefinitions.isEmpty() || !tableDefinition.foreignKeyDefinitions.isEmpty()) {
                    // We do not crash here as to interfere with instantiation. We will display log in error
                    manager.getMessager().printMessage(Diagnostic.Kind.ERROR, "MODEL VIEWS CANNOT HAVE PRIMARY KEYS OR FOREIGN KEYS");
                }

                tableCreationQuery.appendList(mColumnDefinitions).append("););");
                javaWriter.emitStatement("return \"%1s\"", tableCreationQuery.getQuery());
            }
        }, "String", "getCreationQuery", Sets.newHashSet(Modifier.PUBLIC));
    }
}
