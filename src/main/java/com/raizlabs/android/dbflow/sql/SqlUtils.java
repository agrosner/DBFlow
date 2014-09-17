package com.raizlabs.android.dbflow.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.ReflectionUtils;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.ForeignKeyConverter;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.runtime.DatabaseManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.sql.builder.WhereQueryBuilder;
import com.raizlabs.android.dbflow.structure.Column;
import com.raizlabs.android.dbflow.structure.ColumnType;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.StructureUtils;
import com.raizlabs.android.dbflow.structure.TableStructure;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class SqlUtils {

    /**
     * This marks the {@link #save(com.raizlabs.android.dbflow.structure.Model, boolean, int)} operation
     * as checking to see if the model exists before saving.
     */
    public static int SAVE_MODE_DEFAULT = 0;

    /**
     * This marks the {@link #save(com.raizlabs.android.dbflow.structure.Model, boolean, int)} operation
     * as updating only without checking for it to exist. This is when we know the data exists.
     */
    public static int SAVE_MODE_UPDATE = 1;

    /**
     * This marks the {@link #save(com.raizlabs.android.dbflow.structure.Model, boolean, int)} operation
     * as inserting only without checking for it to exist. This is for when we know the data is new.
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
     * @param <ModelClass> The class implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return a list of {@link ModelClass}
     */
    public static <ModelClass extends Model> List<ModelClass> queryList(Class<ModelClass> modelClass, String sql, String... args) {
        Cursor cursor = FlowManager.getWritableDatabase().rawQuery(sql, args);
        List<ModelClass> list = convertToList(modelClass, cursor);
        cursor.close();
        return list;
    }

    /**
     * Queries the DB and returns the first {@link com.raizlabs.android.dbflow.structure.Model} it finds. Note:
     * this may return more than one object, but only will return the first item in the list.
     *
     * @param modelClass   The class to construct the data from the DB into
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         You may include ?s in where clause in the query,
     *                     which will be replaced by the values from selectionArgs. The
     *                     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return a single {@link ModelClass}
     */
    public static <ModelClass extends Model> ModelClass querySingle(Class<ModelClass> modelClass, String sql, String... args) {
        Cursor cursor = FlowManager.getWritableDatabase().rawQuery(sql, args);
        ModelClass retModel = convertToModel(modelClass, cursor);
        cursor.close();
        return retModel;
    }

    public static <ModelClass extends Model> boolean hasData(Class<ModelClass> modelClass, String sql, String...args) {
        Cursor cursor = FlowManager.getWritableDatabase().rawQuery(sql, args);
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

        try {
            Constructor<ModelClass> entityConstructor = table.getConstructor();

            //enable private constructors
            entityConstructor.setAccessible(true);

            if (cursor.moveToFirst()) {
                do {
                    ModelClass model = entityConstructor.newInstance();
                    model.load(cursor);
                    entities.add(model);
                }
                while (cursor.moveToNext());
            }

        } catch (IllegalArgumentException i) {
            throw new RuntimeException("Default constructor for: " + table.getName() + " was not found.");
        } catch (Exception e) {
            FlowLog.log(FlowLog.Level.E, "Failed to process cursor.", e);
        }

        return entities;
    }

    /**
     * Takes first {@link ModelClass} from the cursor
     *
     * @param table        The model class that we convert the cursor data into.
     * @param cursor       The cursor from the DB
     * @param <ModelClass>
     * @return
     */
    public static <ModelClass extends Model> ModelClass convertToModel(Class<ModelClass> table, Cursor cursor) {
        ModelClass model = null;
        try {
            Constructor<ModelClass> entityConstructor = table.getConstructor();

            //enable private constructors
            entityConstructor.setAccessible(true);

            if (cursor.moveToFirst()) {
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

    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void save(ModelClass model, boolean async, int mode) {
        if (!async) {
            WhereQueryBuilder<ModelClass> primaryWhereQueryBuilder = (WhereQueryBuilder<ModelClass>)
                    FlowManager.getCache().getStructure().getPrimaryWhereQuery(model.getClass());


            final SQLiteDatabase db = FlowManager.getWritableDatabase();
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
                        final TypeConverter typeSerializer = FlowManager.getCache()
                                .getStructure().getTypeConverterForClass(fieldType);
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
                        ForeignKeyConverter foreignKeyConverter = FlowManager.getCache().getStructure().getForeignKeyConverterForclass(entityType);
                        values.put(fieldName, foreignKeyConverter.getDBValue((Model) value));
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
                exists = exists(model);
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
                    if (column != null && column.columnType().type() == ColumnType.PRIMARY_KEY_AUTO_INCREMENT) {
                        field.setAccessible(true);
                        try {
                            field.set(mode, id);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } else {
            DatabaseManager.getSharedInstance().saveOnSaveQueue(model);
        }
    }

    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void loadFromCursor(ModelClass model, Cursor cursor) {
        TableStructure<ModelClass> tableStructure = FlowManager.getTableStructureForClass((Class<ModelClass>)model.getClass());
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
                TypeConverter typeSerializer = FlowManager.getCache()
                        .getStructure().getTypeConverterForClass(fieldType);
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
                    ForeignKeyConverter converter = FlowManager.getCache().getStructure().getForeignKeyConverterForclass(entityType);

                    // Build the primary key query using the converter and querybuilder
                    WhereQueryBuilder whereQueryBuilder = FlowManager.getCache().getStructure().getPrimaryWhereQuery(entityType);
                    value = new Select().from(entityType).where()
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

    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void delete(final ModelClass model, boolean async) {
        if (!async) {
            TableStructure tableStructure = FlowManager.getTableStructureForClass(model.getClass());
            WhereQueryBuilder<ModelClass> whereQueryBuilder = FlowManager.getCache().getStructure().getPrimaryWhereQuery((Class<ModelClass>) model.getClass());
            FlowManager.getWritableDatabase().delete(tableStructure.getTableName(), WhereQueryBuilder.getPrimaryModelWhere(whereQueryBuilder, model), null);
        } else {
            DatabaseManager.getSharedInstance().addTransaction(new BaseTransaction<ModelClass>() {
                @Override
                public ModelClass onExecute() {
                    model.delete(false);
                    return model;
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> boolean exists(ModelClass model) {
        WhereQueryBuilder<ModelClass> whereQueryBuilder = (WhereQueryBuilder<ModelClass>) FlowManager.getCache().getStructure().getPrimaryWhereQuery(model.getClass());
        return new Select().from(model.getClass()).where().whereClause(WhereQueryBuilder.getPrimaryModelWhere(whereQueryBuilder, model)).hasData();
    }
}
