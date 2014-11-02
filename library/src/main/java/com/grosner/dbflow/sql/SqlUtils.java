package com.grosner.dbflow.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.IntDef;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.config.BaseFlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.process.DeleteModelListTransaction;
import com.grosner.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.grosner.dbflow.sql.language.Delete;
import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.BaseModelView;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the {@link com.grosner.dbflow.structure.Model} class and let any class use these.
 */
public class SqlUtils {

    public @IntDef @interface SaveMode{}

    /**
     * This marks the {@link #save(com.grosner.dbflow.structure.Model, boolean, int)}
     * operation as checking to see if the model exists before saving.
     */
    public static final @SaveMode int SAVE_MODE_DEFAULT = 0;

    ;
    /**
     * This marks the {@link #save(com.grosner.dbflow.structure.Model, boolean, int)}
     * operation as updating only without checking for it to exist. This is when we know the data exists.
     */
    public static final @SaveMode int SAVE_MODE_UPDATE = 1;
    /**
     * This marks the {@link #save(com.grosner.dbflow.structure.Model, boolean, int)}
     * operation as inserting only without checking for it to exist. This is for when we know the data is new.
     */
    public static final @SaveMode int SAVE_MODE_INSERT = 2;

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
        BaseFlowManager flowManager = FlowManager.getManagerForTable(modelClass);
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        List<ModelClass> list = convertToList(modelClass, cursor);
        cursor.close();
        return list;
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
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelClass convertToModel(boolean isList, Class<ModelClass> table, Cursor cursor) {
        ModelClass model = null;
        try {
            if (isList || cursor.moveToFirst()) {
                ModelAdapter<ModelClass> modelAdapter = FlowManager.getModelAdapter(table);
                if(modelAdapter == null) {
                    Class persistentClass = (Class) ((ParameterizedType) table.getGenericSuperclass()).getActualTypeArguments()[0];
                    if(persistentClass.isAssignableFrom(BaseModelView.class)) {
                        model = (ModelClass) FlowManager.getModelViewAdapter((Class<? extends BaseModelView<? extends Model>>)table).loadFromCursor(cursor);
                    }
                } else {
                    model = modelAdapter.loadFromCursor(cursor);
                }
            }
        } catch (Exception e) {
            FlowLog.log(FlowLog.Level.E, "Failed to process cursor.", e);
        }

        return model;
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
        BaseFlowManager flowManager = FlowManager.getManagerForTable(modelClass);
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        boolean hasData = (cursor.getCount() > 0);
        cursor.close();
        return hasData;
    }

    /**
     * Syncs the model to the database depending on it's save mode.
     * @param async
     * @param model
     * @param contentValues
     * @param mode
     * @param <ModelClass>
     */
    public static <ModelClass extends Model> void sync(boolean async, ModelClass model, ContentValues contentValues, @SaveMode int mode) {
        if (!async) {
            BaseFlowManager flowManager = FlowManager.getManagerForTable(model.getClass());
            ModelAdapter<ModelClass> modelAdapter = (ModelAdapter<ModelClass>) FlowManager.getModelAdapter(model.getClass());
            final SQLiteDatabase db = flowManager.getWritableDatabase();

            boolean exists = false;
            BaseModel.Action action = BaseModel.Action.SAVE;
            if (mode == SAVE_MODE_DEFAULT) {
                exists = modelAdapter.exists(model);
            } else if (mode == SAVE_MODE_UPDATE) {
                exists = true;
                action = BaseModel.Action.UPDATE;
            } else {
                action = BaseModel.Action.INSERT;
            }

            if (exists) {
                exists = (db.update(modelAdapter.getTableName(), contentValues,modelAdapter.getPrimaryModelWhere(model).getQuery(), null) != 0);
            }

            if (!exists) {
                long id = db.insert(modelAdapter.getTableName(), null, contentValues);

                // TODO: add a method for primary increment fields
                /*Collection<Field> primaryFields = flowManager.getTableStructureForClass(model.getClass()).getPrimaryKeys();
                for (Field field : primaryFields) {
                    if (StructureUtils.isPrimaryKeyAutoIncrement(field)) {
                        field.setAccessible(true);
                        try {
                            field.set(mode, id);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }*/
            }

            notifyModelChanged(model.getClass(), action);
        } else {
            TransactionManager.getInstance().save(ProcessModelInfo.withModels(model).info(DBTransactionInfo.create()));
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
    /*@SuppressWarnings("unchecked")
    public static Object getModelValueFromCursor(Cursor cursor, TableStructure tableStructure, Field field, String columnName, Class<?> fieldType) {
        int columnIndex = TextUtils.isEmpty(columnName) ? -1 : cursor.getColumnIndex(columnName);

        Object value = null;

        if (columnIndex >= 0 || (StructureUtils.isForeignKey(field) && ReflectionUtils.implementsModel(fieldType))) {

            boolean columnIsNull = cursor.isNull(columnIndex);
            TypeConverter typeSerializer = FlowManager.getTypeConverterForClass(fieldType);

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

                final Class<? extends Model> entityType = (Class<? extends Model>) fieldType;
                Column foreignKey = field.getAnnotation(Column.class);

                Object[] foreignColumns = new Object[foreignKey.references().length];
                for (int i = 0; i < foreignColumns.length; i++) {
                    ForeignKeyReference foreignKeyReference = foreignKey.references()[i];
                    foreignColumns[i] = getModelValueFromCursor(cursor, tableStructure, null, foreignKeyReference.columnName(), foreignKeyReference.columnType());
                }

                // Build the primary key query using the converter and querybuilder
                ConditionQueryBuilder conditionQueryBuilder = FlowManager.getPrimaryWhereQuery(entityType);
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
    }*/

   /***
     * Deletes {@link com.grosner.dbflow.structure.Model} from the database using the specfied {@link com.grosner.dbflow.config.FlowManager}
     *
     * @param model        The model to delete
     * @param async        Whether it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void   delete(final ModelClass model, ModelAdapter<ModelClass> modelAdapter, boolean async) {
        if (!async) {
            new Delete().from((Class<ModelClass>)model.getClass()).where(modelAdapter.getPrimaryModelWhere(model)).query();
            notifyModelChanged(model.getClass(), BaseModel.Action.DELETE);
        } else {
            TransactionManager.getInstance().addTransaction(new DeleteModelListTransaction<ModelClass>(ProcessModelInfo.withModels(model).fetch()));
        }
    }

    /**
     * Notifies the {@link android.database.ContentObserver} that the model has changed.
     *
     * @param model
     */
    public static void notifyModelChanged(Class<? extends Model> modelClass, BaseModel.Action action) {
        FlowManager.getContext().getContentResolver().notifyChange(getNotificationUri(modelClass, action), null, true);
    }

    /**
     * Returns the uri for notifications  from model changes
     *
     * @param modelClass
     * @return
     */
    public static Uri getNotificationUri(Class<? extends Model> modelClass, BaseModel.Action action) {
        String mode = "";
        if (action != null) {
            mode = "#" + action.name();
        }
        return Uri.parse("dbflow://" + FlowManager.getTableName(modelClass) + mode);
    }
}
