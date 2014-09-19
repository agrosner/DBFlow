package com.grosner.dbflow.structure;

import com.grosner.dbflow.ReflectionUtils;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.sql.builder.TableCreationQueryBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class defines the structure of this table for reference later. It holds information
 * such as the type of {@link com.grosner.dbflow.structure.Model}, the tablename, the column names,
 * primary keys, and foreign keys.
 */
public class TableStructure<ModelType extends Model> {

    /**
     * The Model class this table connects to
     */
    private Class<ModelType> mModelType;

    /**
     * The name of the table. Can be different from the class name
     */
    private String mTableName;

    /**
     * The mapping between the fields of a {@link com.grosner.dbflow.structure.Model}
     * and the name of the columns
     */
    private Map<Field, String> mColumnNames;

    private Map<String, Field> mFieldFromNames;

    /**
     * The primary keys of this table. They must not be empty.
     */
    private LinkedHashMap<String, Field> mPrimaryKeys;

    /**
     * The foreign keys of this table.
     */
    private LinkedHashMap<String, Field> mForeignKeys;

    /**
     * Holds the Creation Query in this table for reference when we create it
     */
    private TableCreationQueryBuilder mCreationQuery;

    /**
     * Manages the DB that this structure is in
     */
    private FlowManager mManager;

    /**
     * Builds the structure of this table based on the {@link com.grosner.dbflow.structure.Model}
     * class passed in.
     *
     * @param modelType
     */
    public TableStructure(FlowManager flowManager, Class<ModelType> modelType) {
        mManager = flowManager;
        mColumnNames = new HashMap<Field, String>();
        mFieldFromNames = new HashMap<String, Field>();
        mPrimaryKeys = new LinkedHashMap<String, Field>();
        mForeignKeys = new LinkedHashMap<String, Field>();

        mModelType = modelType;

        Table table = mModelType.getAnnotation(Table.class);
        if (table != null) {
            mTableName = table.name();
        } else {
            mTableName = mModelType.getSimpleName();
        }

        List<Field> fields = new ArrayList<Field>();
        fields = ReflectionUtils.getAllColumns(fields, mModelType);

        // Generating creation query on the fly to be processed later
        mCreationQuery = new TableCreationQueryBuilder();
        mCreationQuery.appendCreateTableIfNotExists(mTableName);

        ArrayList<QueryBuilder> mColumnDefinitions = new ArrayList<QueryBuilder>();

        // Loop through fields and store their corresponding field names
        // Also determine if its primary key or foreign key
        for (Field field : fields) {
            TableCreationQueryBuilder tableCreationQuery = new TableCreationQueryBuilder();
            Class type = field.getType();

            String columnName;
            Column column = field.getAnnotation(Column.class);
            if (column.name() != null && !column.name().equals("")) {
                columnName = column.name();
            } else {
                columnName = field.getName();
            }

            mColumnNames.put(field, columnName);
            mFieldFromNames.put(columnName, field);

            if (column.value().value() == ColumnType.PRIMARY_KEY
                    || column.value().value() == ColumnType.PRIMARY_KEY_AUTO_INCREMENT) {
                mPrimaryKeys.put(columnName, field);
            } else if (column.value().value() == ColumnType.FOREIGN_KEY) {
                mForeignKeys.put(columnName, field);
            }

            if (SQLiteType.containsClass(type)) {
                tableCreationQuery.append(columnName)
                        .appendSpace()
                        .appendType(type);
            } else if (ReflectionUtils.isSubclassOf(type, Enum.class)) {
                tableCreationQuery.append(columnName)
                        .appendSpace()
                        .appendSQLiteType(SQLiteType.TEXT);
            }

            mColumnDefinitions.add(tableCreationQuery.appendColumn(column));
        }

        if (mPrimaryKeys.isEmpty()) {
            throw new PrimaryKeyNotFoundException("Table: " + mTableName + " must define a primary key");
        }

        QueryBuilder primaryKeyQueryBuilder = new QueryBuilder().append("PRIMARY KEY(");
        Collection<Field> primaryKeys = getPrimaryKeys();
        int count = 0;
        int index = 0;
        for (Field field : primaryKeys) {
            Column primaryKey = field.getAnnotation(Column.class);
            if (primaryKey.value().value() != ColumnType.PRIMARY_KEY_AUTO_INCREMENT) {
                count++;
                primaryKeyQueryBuilder.append(mColumnNames.get(field));
                if (index < mPrimaryKeys.size() - 1) {
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
        Collection<Field> foreignKeys = getForeignKeys();
        for (Field foreignKeyField : foreignKeys) {
            foreignKeyQueryBuilder = new QueryBuilder().append("FOREIGN KEY(");

            Column foreignKey = foreignKeyField.getAnnotation(Column.class);

            foreignKeyQueryBuilder.append(mColumnNames.get(foreignKeyField))
                    .append(")").appendSpaceSeparated("REFERENCES")
                    .append(mTableName)
                    .append("(").append(foreignKey.foreignColumn()).append(")");

            mColumnDefinitions.add(foreignKeyQueryBuilder);
        }


        mCreationQuery.appendColumnDefinitions(mColumnDefinitions).append(");");

    }

    /**
     * Returns the query that we use to create this table.
     *
     * @return
     */
    public QueryBuilder getCreationQuery() {
        return mCreationQuery;
    }

    /**
     * Returns this table name
     *
     * @return
     */
    public String getTableName() {
        return mTableName;
    }

    /**
     * Returns the column name for the specified field.
     *
     * @param field
     * @return
     */
    public String getColumnName(Field field) {
        return mColumnNames.get(field);
    }

    /**
     * Returns all of the field columns for this table
     *
     * @return
     */
    public Set<Field> getColumns() {
        return mColumnNames.keySet();
    }

    /**
     * Returns all of the foreign keys for this table
     *
     * @return
     */
    public Collection<Field> getForeignKeys() {
        return mForeignKeys.values();
    }

    /**
     * Returns all of the primary keys for this table
     *
     * @return
     */
    public Collection<Field> getPrimaryKeys() {
        return mPrimaryKeys.values();
    }

    /**
     * Returns the list of primary column key names
     *
     * @return
     */
    public Set<String> getPrimaryKeyNames() {
        return mPrimaryKeys.keySet();
    }

    /**
     * Returns the field for the column name
     *
     * @param name
     * @return
     */
    public Field getField(String name) {
        return mFieldFromNames.get(name);
    }

    /**
     * Returns the model that this table corresponds to
     *
     * @return
     */
    public Class<ModelType> getModelType() {
        return mModelType;
    }

    /**
     * Returns the database manager for this table
     *
     * @return
     */
    public FlowManager getManager() {
        return mManager;
    }
}
