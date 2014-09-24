package com.grosner.dbflow.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.ReflectionUtils;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.ForeignKeyConverter;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.BaseTransaction;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.ColumnType;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.StructureUtils;
import com.grosner.dbflow.structure.TableStructure;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the {@link com.grosner.dbflow.structure.Model} class and let any class use these.
 */
public class SqlUtils {

    /**
     * This marks the {@link #save(com.grosner.dbflow.config.FlowManager, com.grosner.dbflow.structure.Model, boolean, int)}
     * operation as checking to see if the model exists before saving.
     */
    public static int SAVE_MODE_DEFAULT = 0;

    /**
     * This marks the {@link #save(com.grosner.dbflow.config.FlowManager, com.grosner.dbflow.structure.Model, boolean, int)}
     * operation as updating only without checking for it to exist. This is when we know the data exists.
     */
    public static int SAVE_MODE_UPDATE = 1;

    /**
     * This marks the {@link #save(com.grosner.dbflow.config.FlowManager, com.grosner.dbflow.structure.Model, boolean, int)}
     * operation as inserting only without checking for it to exist. This is for when we know the data is new.
     */
    public static int SAVE_MODE_INSERT = 2;

    /**
     * Queries the DB for a {@link android.database.Cursor} and converts it into a list.
     *
     * @param modelClass   The class to construct the data from the DB into
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         You may include ?s in where clause in the query,
     *                     which will be replaced by the values from selectionArgs. The
     *                     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link com.grosner.dbflow.structure.Model}
     * @return a list of {@link ModelClass}
     */
    public static <ModelClass extends Model> List<ModelClass> queryList(FlowManager flowManager, Class<ModelClass> modelClass, String sql, String... args) {
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        List<ModelClass> list = convertToList(flowManager, modelClass, cursor);
        cursor.close();
        return list;
    }

    /**
     * Queries the DB and returns the first {@link com.grosner.dbflow.structure.Model} it finds. Note:
     * this may return more than one object, but only will return the first item in the list.
     *
     * @param modelClass   The class to construct the data from the DB into
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         You may include ?s in where clause in the query,
     *                     which will be replaced by the values from selectionArgs. The
     *                     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link com.grosner.dbflow.structure.Model}
     * @return a single {@link ModelClass}
     */
    public static <ModelClass extends Model> ModelClass querySingle(FlowManager flowManager, Class<ModelClass> modelClass, String sql, String... args) {
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        ModelClass retModel = convertToModel(false, flowManager, modelClass, cursor);
        cursor.close();
        return retModel;
    }

    /**
     * Checks whether the SQL query returns a {@link android.database.Cursor} with a count of at least 1. This
     * means that the query was successful. It is commonly used when checking if a {@link com.grosner.dbflow.structure.Model} exists.
     *
     * @param flowManager  The database manager that we run this query on
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         The optional string arguments when we use "?" in the sql
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return
     */
    public static <ModelClass extends Model> boolean hasData(FlowManager flowManager, Class<ModelClass> modelClass, String sql, String... args) {
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        boolean hasData = (cursor.getCount() > 0);
        cursor.close();
        return hasData;
    }

    /**
     * Loops through a cursor and builds a list of {@link ModelClass} objects.
     *
     * @param table        The model class that we convert the cursor data into.
     * @param cursor       The cursor from the DB
     * @param <ModelClass>
     * @return
     */
    public static <ModelClass extends Model> List<ModelClass> convertToList(FlowManager flowManager, Class<ModelClass> table, Cursor cursor) {
        final List<ModelClass> entities = new ArrayList<ModelClass>();

        if (cursor.moveToFirst()) {
            do {
                entities.add(convertToModel(true, flowManager, table, cursor));
            }
            while (cursor.moveToNext());
        }

        return entities;
    }

    /**
     * Takes first {@link ModelClass} from the cursor
     *
     * @param isList       If it's a list, do not reset the cursor
     * @param flowManager  The database manager that we run this query on
     * @param table        The model class that we convert the cursor data into.
     * @param cursor       The cursor from the DB
     * @param <ModelClass>
     * @return
     */
    public static <ModelClass extends Model> ModelClass convertToModel(boolean isList, FlowManager flowManager, Class<ModelClass> table, Cursor cursor) {
        ModelClass model = null;
        try {
            Constructor<ModelClass> entityConstructor = flowManager.getStructure().getConstructorForModel(table);
            if (isList || cursor.moveToFirst()) {
                model = entityConstructor.newInstance();
                model.load(cursor);
            }

        } catch (IllegalArgumentException i) {
            throw new RuntimeException("Default constructor for: " + table.getName() + " was not found.");
        } catch (Exception e) {
            FlowLog.log(FlowLog.Level.E, "Failed to process cursor.", e);
        }

        return model;
    }

    /**
     * Saves a model to the database.
     *
     * @param flowManager  The database manager that we run this query on
     * @param model        The model to save
     * @param async        Whether it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param mode         The save mode, can be {@link #SAVE_MODE_DEFAULT}, {@link #SAVE_MODE_INSERT}, {@link #SAVE_MODE_UPDATE}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void save(FlowManager flowManager, ModelClass model, boolean async, int mode, boolean notify) {
        if (!async) {
            WhereQueryBuilder<ModelClass> primaryWhereQueryBuilder =
                    flowManager.getStructure().getPrimaryWhereQuery((Class<ModelClass>) model.getClass());

            final SQLiteDatabase db = flowManager.getWritableDatabase();
            final ContentValues values = new ContentValues();

            TableStructure<ModelClass> tableStructure = primaryWhereQueryBuilder.getTableStructure();

            Set<Field> fields = tableStructure.getColumns();
            for (Field field : fields) {
                String fieldName = tableStructure.getColumnName(field);
                Class<?> fieldType = field.getType();

                field.setAccessible(true);

                try {
                    Object value = field.get(model);

                    if (value != null) {
                        final TypeConverter typeSerializer = flowManager.getTypeConverterForClass(fieldType);
                        if (typeSerializer != null) {
                            // serialize data
                            value = typeSerializer.getDBValue(value);
                            // set new object type
                            if (value != null) {
                                fieldType = value.getClass();
                                // check that the serializer returned what it promised
                                if (!fieldType.equals(typeSerializer.getDatabaseType())) {
                                    FlowLog.log(FlowLog.Level.W, String.format(TypeConverter.class.getSimpleName() + " returned wrong type: expected a %s but got a %s",
                                            typeSerializer.getDatabaseType(), fieldType));
                                }
                            }
                        }
                    }

                    // TODO: Find a smarter way to do this? This if block is necessary because we
                    // can't know the type until runtime.
                    if (value == null) {
                        values.putNull(fieldName);
                    } else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
                        values.put(fieldName, (Byte) value);
                    } else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
                        values.put(fieldName, (Short) value);
                    } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                        values.put(fieldName, (Integer) value);
                    } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                        values.put(fieldName, (Long) value);
                    } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                        values.put(fieldName, (Float) value);
                    } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                        values.put(fieldName, (Double) value);
                    } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                        values.put(fieldName, (Boolean) value);
                    } else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
                        values.put(fieldName, value.toString());
                    } else if (fieldType.equals(String.class)) {
                        values.put(fieldName, value.toString());
                    } else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
                        values.put(fieldName, (byte[]) value);
                    } else if (StructureUtils.isForeignKey(field) && ReflectionUtils.implementsModel(fieldType)) {
                        Column key = field.getAnnotation(Column.class);
                        if (!key.name().equals("")) {
                            fieldName = field.getAnnotation(Column.class).name();
                        }
                        Class<? extends Model> entityType = (Class<? extends Model>) fieldType;
                        ForeignKeyConverter foreignKeyConverter = flowManager.getStructure().getForeignKeyConverterForclass(entityType);
                        values.put(fieldName, foreignKeyConverter.getDBValue(flowManager, (Model) value));
                    } else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
                        values.put(fieldName, ((Enum<?>) value).name());
                    }
                } catch (IllegalArgumentException e) {
                    FlowLog.logError(e);
                } catch (IllegalAccessException e) {
                    FlowLog.logError(e);
                }
            }

            boolean exists = false;
            if (mode == SAVE_MODE_DEFAULT) {
                exists = exists(flowManager, model);
            } else if (mode == SAVE_MODE_UPDATE) {
                exists = true;
            }

            if (exists) {
                exists = (db.update(tableStructure.getTableName(), values,
                        WhereQueryBuilder.getPrimaryModelWhere(primaryWhereQueryBuilder, model), null) != 0);
            }

            if (!exists) {
                long id = db.insert(tableStructure.getTableName(), null, values);
                for (Field field : fields) {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null && column.value().value() == ColumnType.PRIMARY_KEY_AUTO_INCREMENT) {
                        field.setAccessible(true);
                        try {
                            field.set(mode, id);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            if (notify) {
                // Notify any observers of this model change
                flowManager.getStructure().fireModelChanged(model);
            }

        } else {
            TransactionManager.getInstance().save(DBTransactionInfo.create(), model);
        }
    }

    /**
     * Loads a {@link com.grosner.dbflow.structure.Model} from the DB cursor through reflection with the
     * specified {@link com.grosner.dbflow.config.FlowManager}.
     *
     * @param flowManager  The database manager that we run this query on
     * @param model        The model we load from the cursor
     * @param cursor       The cursor from the DB
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void loadFromCursor(FlowManager flowManager, ModelClass model, Cursor cursor) {
        TableStructure<ModelClass> tableStructure = flowManager.getTableStructureForClass((Class<ModelClass>) model.getClass());
        Set<Field> fields = tableStructure.getColumns();
        for (Field field : fields) {
            final String fieldName = tableStructure.getColumnName(field);
            Class<?> fieldType = field.getType();
            final int columnIndex = cursor.getColumnIndex(fieldName);

            if (columnIndex < 0) {
                continue;
            }

            field.setAccessible(true);

            try {
                boolean columnIsNull = cursor.isNull(columnIndex);
                TypeConverter typeSerializer = flowManager.getTypeConverterForClass(fieldType);
                Object value = null;

                if (typeSerializer != null) {
                    fieldType = typeSerializer.getDatabaseType();
                }

                // TODO: Find a smarter way to do this? This if block is necessary because we
                // can't know the type until runtime.
                if (columnIsNull) {
                    field = null;
                } else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
                    value = cursor.getInt(columnIndex);
                } else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
                    value = cursor.getInt(columnIndex);
                } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                    value = cursor.getInt(columnIndex);
                } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                    value = cursor.getLong(columnIndex);
                } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                    value = cursor.getFloat(columnIndex);
                } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                    value = cursor.getDouble(columnIndex);
                } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                    value = cursor.getInt(columnIndex) != 0;
                } else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
                    value = cursor.getString(columnIndex).charAt(0);
                } else if (fieldType.equals(String.class)) {
                    value = cursor.getString(columnIndex);
                } else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
                    value = cursor.getBlob(columnIndex);
                } else if (StructureUtils.isForeignKey(field) && ReflectionUtils.implementsModel(fieldType)) {
                    // If field is foreign key, we convert it's id
                    final String entityId = cursor.getString(columnIndex);
                    final Class<? extends Model> entityType = (Class<? extends Model>) fieldType;

                    // Get converter to use for foreign keys
                    ForeignKeyConverter converter = FlowManager.getInstance().getStructure().getForeignKeyConverterForclass(entityType);

                    // Build the primary key query using the converter and querybuilder
                    WhereQueryBuilder whereQueryBuilder = FlowManager.getInstance().getStructure().getPrimaryWhereQuery(entityType);
                    value = new Select(flowManager).from(entityType).where()
                            .whereQuery(whereQueryBuilder.replaceEmptyParams(converter.getForeignKeys(entityId)))
                            .querySingle();
                } else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
                    @SuppressWarnings("rawtypes")
                    final Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;
                    value = Enum.valueOf(enumType, cursor.getString(columnIndex));
                }

                // Use a deserializer if one is available
                if (typeSerializer != null && !columnIsNull) {
                    value = typeSerializer.getModelValue(value);
                }

                // Set the field name
                if (value != null) {
                    field.set(model, value);
                }
            } catch (IllegalArgumentException e) {
                FlowLog.logError(e);
            } catch (IllegalAccessException e) {
                FlowLog.logError(e);
            } catch (SecurityException e) {
                FlowLog.logError(e);
            }
        }
    }

    /**
     * Deletes {@link com.grosner.dbflow.structure.Model} from the database using the specfied {@link com.grosner.dbflow.config.FlowManager}
     *
     * @param flowManager  The database manager that we run this query on
     * @param model        The model to delete
     * @param async        Whether it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void delete(FlowManager flowManager, final ModelClass model, boolean async) {
        if (!async) {
            TableStructure tableStructure = flowManager.getTableStructureForClass(model.getClass());
            WhereQueryBuilder<ModelClass> whereQueryBuilder = flowManager.getStructure().getPrimaryWhereQuery((Class<ModelClass>) model.getClass());
            flowManager.getWritableDatabase().delete(tableStructure.getTableName(), WhereQueryBuilder.getPrimaryModelWhere(whereQueryBuilder, model), null);
        } else {
            TransactionManager.getInstance().addTransaction(new BaseTransaction<ModelClass>() {
                @Override
                public ModelClass onExecute() {
                    model.delete(false);
                    return model;
                }
            });
        }
    }

    /**
     * This is used to check if the specified {@link com.grosner.dbflow.structure.Model} exists within the specified
     * {@link com.grosner.dbflow.config.FlowManager} DB.
     *
     * @param flowManager  The database manager that we run this query on
     * @param model        The model to query
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return If the model exists within the DB
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> boolean exists(FlowManager flowManager, ModelClass model) {
        WhereQueryBuilder<ModelClass> whereQueryBuilder = (WhereQueryBuilder<ModelClass>) flowManager.getStructure().getPrimaryWhereQuery(model.getClass());
        return new Select(flowManager).from(model.getClass()).where().whereClause(WhereQueryBuilder.getPrimaryModelWhere(whereQueryBuilder, model)).hasData();
    }
}
