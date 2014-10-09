package com.grosner.dbflow.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.ReflectionUtils;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.observer.ModelObserver;
import com.grosner.dbflow.runtime.transaction.process.DeleteModelListTransaction;
import com.grosner.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.ForeignKeyReference;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.StructureUtils;
import com.grosner.dbflow.structure.TableStructure;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
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
    public static final int SAVE_MODE_DEFAULT = 0;

    /**
     * This marks the {@link #save(com.grosner.dbflow.config.FlowManager, com.grosner.dbflow.structure.Model, boolean, int)}
     * operation as updating only without checking for it to exist. This is when we know the data exists.
     */
    public static final int SAVE_MODE_UPDATE = 1;

    /**
     * This marks the {@link #save(com.grosner.dbflow.config.FlowManager, com.grosner.dbflow.structure.Model, boolean, int)}
     * operation as inserting only without checking for it to exist. This is for when we know the data is new.
     */
    public static final int SAVE_MODE_INSERT = 2;

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
    public static <ModelClass extends Model> List<ModelClass> queryList(Class<ModelClass> modelClass, String sql, String... args) {
        FlowManager flowManager = FlowManager.getManagerForTable(modelClass);
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        List<ModelClass> list = convertToList(modelClass, cursor);
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
    public static <ModelClass extends Model> ModelClass querySingle(Class<ModelClass> modelClass, String sql, String... args) {
        Cursor cursor = FlowManager.getManagerForTable(modelClass).getWritableDatabase().rawQuery(sql, args);
        ModelClass retModel = convertToModel(false, modelClass, cursor);
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
    public static <ModelClass extends Model> boolean hasData(Class<ModelClass> modelClass, String sql, String... args) {
        FlowManager flowManager = FlowManager.getManagerForTable(modelClass);
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
    public static <ModelClass extends Model> List<ModelClass> convertToList(Class<ModelClass> table, Cursor cursor) {
        final List<ModelClass> entities = new ArrayList<ModelClass>();

        if (cursor.moveToFirst()) {
            do {
                entities.add(convertToModel(true, table, cursor));
            }
            while (cursor.moveToNext());
        }

        return entities;
    }

    /**
     * Takes first {@link ModelClass} from the cursor
     *
     * @param isList       If it's a list, do not reset the cursor
     * @param table        The model class that we convert the cursor data into.
     * @param cursor       The cursor from the DB
     * @param <ModelClass>
     * @return
     */
    public static <ModelClass extends Model> ModelClass convertToModel(boolean isList, Class<ModelClass> table, Cursor cursor) {
        ModelClass model = null;
        try {
            Constructor<ModelClass> entityConstructor = FlowManager.getManagerForTable(table).getStructure().getConstructorForModel(table);
            if (isList || cursor.moveToFirst()) {
                model = entityConstructor.newInstance();
                model.load(cursor);
            }

        } catch (IllegalArgumentException i) {
            throw new RuntimeException("Default constructor for: " + table.getName() + " was not found.");
        } catch (NoSuchMethodException n) {
            throw new RuntimeException("Default constructor for: " + table.getName() + " was not found.");
        } catch (Exception e) {
            FlowLog.log(FlowLog.Level.E, "Failed to process cursor.", e);
        }

        return model;
    }

    @SuppressWarnings("unchecked")
    public static void putField(ContentValues values, FlowManager flowManager, Field field, String fieldName, Object value) {
        Class<?> fieldType = field.getType();

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
            Class<? extends Model> entityType = (Class<? extends Model>) fieldType;

            TableStructure tableStructure = flowManager.getStructure().getTableStructureForClass(entityType);
            for(ForeignKeyReference foreignKeyReference: key.references()) {
                Field foreignColumnField = tableStructure.getField(foreignKeyReference.foreignColumnName());
                foreignColumnField.setAccessible(true);
                try {
                    putField(values, flowManager, foreignColumnField, foreignKeyReference.columnName(),
                            foreignColumnField.get(value));
                } catch (IllegalAccessException e) {
                    FlowLog.logError(e);
                }
            }
        } else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
            values.put(fieldName, ((Enum<?>) value).name());
        }
    }

    /**
     * Saves a model to the database.
     *
     * @param model        The model to save
     * @param async        Whether it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param mode         The save mode, can be {@link #SAVE_MODE_DEFAULT}, {@link #SAVE_MODE_INSERT}, {@link #SAVE_MODE_UPDATE}
     * @param notify       If we should notify all of our {@link com.grosner.dbflow.runtime.observer.ModelObserver}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void save(ModelClass model, boolean async, int mode, boolean notify) {
        if (!async) {
            FlowManager flowManager = FlowManager.getManagerForTable(model.getClass());
            ConditionQueryBuilder<ModelClass> primaryConditionQueryBuilder =
                    flowManager.getStructure().getPrimaryWhereQuery((Class<ModelClass>) model.getClass());

            final SQLiteDatabase db = flowManager.getWritableDatabase();
            final ContentValues values = new ContentValues();

            TableStructure<ModelClass> tableStructure = primaryConditionQueryBuilder.getTableStructure();

            Set<Field> fields = tableStructure.getColumns();
            for (Field field : fields) {
                String fieldName = tableStructure.getColumnName(field);
                field.setAccessible(true);

                try {
                    putField(values, flowManager, field, fieldName, field.get(model));
                } catch (IllegalArgumentException e) {
                    FlowLog.logError(e);
                } catch (IllegalAccessException e) {
                    FlowLog.logError(e);
                }
            }

            boolean exists = false;
            if (mode == SAVE_MODE_DEFAULT) {
                exists = exists(model);
            } else if (mode == SAVE_MODE_UPDATE) {
                exists = true;
            }

            if (exists) {
                exists = (db.update(tableStructure.getTableName(), values,
                        ConditionQueryBuilder.getPrimaryModelWhere(primaryConditionQueryBuilder, model), null) != 0);
            }

            if (!exists) {
                long id = db.insert(tableStructure.getTableName(), null, values);

                Collection<Field> primaryFields = tableStructure.getPrimaryKeys();
                for (Field field : primaryFields) {
                    if (StructureUtils.isPrimaryKeyAutoIncrement(field)) {
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
                FlowManager.getManagerForTable(model.getClass()).fireModelChanged(model, ModelObserver.Mode.fromData(mode, false));
            }

        } else {
            TransactionManager.getInstance().save(ProcessModelInfo.withModels(model).info(DBTransactionInfo.create()));
        }
    }

    /**
     * Loads a {@link com.grosner.dbflow.structure.Model} from the DB cursor through reflection with the
     * specified {@link com.grosner.dbflow.config.FlowManager}.
     *
     * @param model        The model we load from the cursor
     * @param cursor       The cursor from the DB
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void loadFromCursor(ModelClass model, Cursor cursor) {
        TableStructure<ModelClass> tableStructure = FlowManager.getManagerForTable(model.getClass()).getTableStructureForClass((Class<ModelClass>) model.getClass());
        Set<Field> fields = tableStructure.getColumns();
        for (Field field : fields) {
            try {
                Object value = getModelValueFromCursor(cursor, tableStructure, field);

                // Set the field value
                if (value != null) {
                    field.setAccessible(true);
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
     * Converts the value from the database {@link android.database.Cursor} into the value that goes into a model from the
     * specified {@link com.grosner.dbflow.structure.TableStructure} and {@link java.lang.reflect.Field}.
     *
     * @param cursor         The cursor from the DB
     * @param tableStructure The structure of the table we're on
     * @param field          The field from the {@link com.grosner.dbflow.structure.Model} class
     * @return The value that should be set on the field from the {@link com.grosner.dbflow.structure.TableStructure}
     */
    public static Object getModelValueFromCursor(Cursor cursor, TableStructure tableStructure, Field field) {
        int columnIndex = cursor.getColumnIndex(tableStructure.getColumnName(field));

        Object value = null;

        if (columnIndex >=0 ) {
            Class<?> fieldType = field.getType();
            boolean columnIsNull = cursor.isNull(columnIndex);
            TypeConverter typeSerializer = tableStructure.getManager().getTypeConverterForClass(fieldType);

            if (typeSerializer != null) {
                fieldType = typeSerializer.getDatabaseType();
            }

            if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
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
                Column foreignKey = field.getAnnotation(Column.class);

                String[] foreignColumns = new String[foreignKey.references().length];
                for(int i = 0; i < foreignColumns.length; i++) {
                    foreignColumns[i] = foreignKey.references()[i].foreignColumnName();
                }

                // Build the primary key query using the converter and querybuilder
                ConditionQueryBuilder conditionQueryBuilder = FlowManager.getManagerForTable(tableStructure.getModelType()).getStructure().getPrimaryWhereQuery(entityType);
                value = new Select().from(entityType).where()
                        .whereQuery(conditionQueryBuilder.replaceEmptyParams(foreignColumns))
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
        }

        return value;
    }

    /**
     * Deletes {@link com.grosner.dbflow.structure.Model} from the database using the specfied {@link com.grosner.dbflow.config.FlowManager}
     *
     * @param model        The model to delete
     * @param async        Whether it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void delete(final ModelClass model, boolean async, boolean notify) {
        if (!async) {
            FlowManager flowManager = FlowManager.getManagerForTable(model.getClass());
            TableStructure tableStructure = flowManager.getTableStructureForClass(model.getClass());
            ConditionQueryBuilder<ModelClass> conditionQueryBuilder = flowManager.getStructure().getPrimaryWhereQuery((Class<ModelClass>) model.getClass());
            flowManager.getWritableDatabase().delete(tableStructure.getTableName(), ConditionQueryBuilder.getPrimaryModelWhere(conditionQueryBuilder, model), null);

            if (notify) {
                // Notify any observers of this model change
                FlowManager.getManagerForTable(model.getClass()).fireModelChanged(model, ModelObserver.Mode.fromData(0, true));
            }
        } else {
            TransactionManager.getInstance().addTransaction(new DeleteModelListTransaction<ModelClass>(ProcessModelInfo.withModels(model).fetch()));
        }
    }

    /**
     * This is used to check if the specified {@link com.grosner.dbflow.structure.Model} exists within the specified
     * {@link com.grosner.dbflow.config.FlowManager} DB.
     *
     * @param model        The model to query
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return If the model exists within the DB
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> boolean exists(ModelClass model) {
        ConditionQueryBuilder<ModelClass> conditionQueryBuilder = (ConditionQueryBuilder<ModelClass>)
                FlowManager.getManagerForTable(model.getClass()).getStructure().getPrimaryWhereQuery(model.getClass());
        return new Select().from(model.getClass()).where(ConditionQueryBuilder.getPrimaryModelWhere(conditionQueryBuilder, model)).hasData();
    }
}
