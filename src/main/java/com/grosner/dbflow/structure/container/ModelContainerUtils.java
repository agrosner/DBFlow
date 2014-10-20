package com.grosner.dbflow.structure.container;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.builder.PrimaryKeyCannotBeNullException;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.StructureUtils;
import com.grosner.dbflow.structure.TableStructure;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Description: Provides helper methods for handling {@link com.grosner.dbflow.structure.container.ModelContainer} classes.
 * These wrap around {@link com.grosner.dbflow.structure.Model} to provide more convenient means of interacting with the db.
 */
public class ModelContainerUtils {

    /**
     * Saves a {@link com.grosner.dbflow.structure.container.BaseModelContainer} to the database.
     *
     * @param modelContainer The container model to save
     * @param async          Where it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately/
     * @param mode           The save mode, can be {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_DEFAULT},
     *                       {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_INSERT}, {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_UPDATE}
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public static <ModelClass extends Model> void save(BaseModelContainer<ModelClass, ?> modelContainer, boolean async, @SqlUtils.SaveMode int mode) {
        if (!async) {

            TableStructure<ModelClass> tableStructure = modelContainer.mTableStructure;
            FlowManager flowManager = tableStructure.getManager();

            ConditionQueryBuilder<ModelClass> primaryConditionQueryBuilder = FlowManager.getPrimaryWhereQuery(modelContainer.getTable());

            final SQLiteDatabase db = flowManager.getWritableDatabase();
            final ContentValues values = new ContentValues();

            Set<Field> fields = tableStructure.getColumns();
            for (Field field : fields) {
                String columnName = tableStructure.getColumnName(field);
                Object value = modelContainer.getValue(columnName);
                field.setAccessible(true);
                SqlUtils.putField(values, flowManager, field, columnName, value);
            }

            boolean exists = false;
            BaseModel.Action action = BaseModel.Action.SAVE;
            if (mode == SqlUtils.SAVE_MODE_DEFAULT) {
                exists = exists(modelContainer);
            } else if (mode == SqlUtils.SAVE_MODE_UPDATE) {
                exists = true;
                action = BaseModel.Action.UPDATE;
            } else {
                action = BaseModel.Action.INSERT;
            }

            if (exists) {
                exists = (db.update(tableStructure.getTableName(), values,
                        getModelBackedWhere(primaryConditionQueryBuilder, tableStructure.getPrimaryKeys(), modelContainer), null) != 0);
            }

            if (!exists) {
                long id = db.insert(tableStructure.getTableName(), null, values);

                Collection<Field> primaryKeys = tableStructure.getPrimaryKeys();
                for (Field field : primaryKeys) {
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

            SqlUtils.notifyModelChanged(modelContainer.getTable(), action);

        } else {
            TransactionManager.getInstance().save(ProcessModelInfo.withModels(modelContainer));
        }
    }

    /**
     * Loads a {@link com.grosner.dbflow.structure.Model} from the DB cursor into data from the {@link com.grosner.dbflow.structure.container.ModelContainer}.
     *
     * @param modelContainer The data to load the cursor into
     * @param cursor         The cursor from the DB
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public static <ModelClass extends Model> void loadFromCursor(BaseModelContainer<ModelClass, ?> modelContainer, Cursor cursor) {
        Set<Field> fields = modelContainer.mTableStructure.getColumns();
        for (Field field : fields) {
            Object value = SqlUtils.getModelValueFromCursor(cursor, modelContainer.mTableStructure, field, modelContainer.mTableStructure.getColumnName(field), field.getType());

            if (value != null) {
                modelContainer.put(modelContainer.mTableStructure.getColumnName(field), value);
            }
        }
    }

    /**
     * Converts data into its corresponding {@link ModelClass}.
     *
     * @param modelContainer The model that holds preconverted data
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public static <ModelClass extends Model> ModelClass toModel(BaseModelContainer<ModelClass, ?> modelContainer) {
        Cursor cursor = new Select().from(modelContainer.getTable()).where(getPrimaryModelWhere(modelContainer)).query();
        ModelClass model = SqlUtils.convertToModel(false, modelContainer.getTable(), cursor);
        cursor.close();

        return model;
    }

    /**
     * Deletes {@link com.grosner.dbflow.structure.Model} from the database using the specfied {@link com.grosner.dbflow.config.FlowManager}
     *
     * @param modelContainer The jsonModel that corresponds to an item in the DB we want to delete
     * @param async          Whether it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void delete(final BaseModelContainer<ModelClass, ?> modelContainer, boolean async) {
        if (!async) {
            FlowManager flowManager = FlowManager.getManagerForTable(modelContainer.getTable());
            flowManager.getWritableDatabase().delete(modelContainer.mTableStructure.getTableName(), getPrimaryModelWhere(modelContainer), null);
            SqlUtils.notifyModelChanged(modelContainer.getTable(), BaseModel.Action.DELETE);
        } else {
            TransactionManager.getInstance().delete(ProcessModelInfo.withModels(modelContainer));
        }
    }

    /**
     * Returns whether the given {@link com.grosner.dbflow.structure.container.BaseModelContainer} exists in the DB as the {@link ModelClass}
     * table it points to.
     *
     * @param jsonModel    The json model that points to a {@link com.grosner.dbflow.structure.Model} class
     * @param <ModelClass> The class that implements {@link ModelClass}
     * @return If the model that the {@link JSONModel} points to exists in the DB
     */
    public static <ModelClass extends Model> boolean exists(BaseModelContainer<ModelClass, ?> jsonModel) {
        return new Select().from(jsonModel.getTable())
                .where(getPrimaryModelWhere(jsonModel)).hasData();
    }

    /**
     * Builds a {@link com.grosner.dbflow.structure.Model} where query with its primary keys.
     *
     * @param modelContainer The existing model with all of its primary keys filled in
     * @return
     */
    public static <ModelClass extends Model> String getPrimaryModelWhere(BaseModelContainer<ModelClass, ?> modelContainer) {
        ConditionQueryBuilder<ModelClass> existing = FlowManager.getPrimaryWhereQuery(modelContainer.getTable());
        return getModelBackedWhere(existing, existing.getTableStructure().getPrimaryKeys(), modelContainer);
    }

    /**
     * Returns a where query String from the existing builder and collection of fields. This is only different
     * than {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder#getModelBackedWhere(com.grosner.dbflow.sql.builder.ConditionQueryBuilder, java.util.Collection, com.grosner.dbflow.structure.Model)}
     * in that we call {@link org.json.JSONObject#get(String)} for the value of the field.
     *
     * @param existing       The existing where query we wish to generate new one from the model.
     * @param fields         The list of fields that we look up the column names for
     * @param modelContainer The {@link com.grosner.dbflow.structure.container.BaseModelContainer} to get the field values from
     * @param <ModelClass>
     * @return
     */
    public static <ModelClass extends Model> String getModelBackedWhere(ConditionQueryBuilder<ModelClass> existing,
                                                                        Collection<Field> fields, BaseModelContainer<ModelClass, ?> modelContainer) {
        String query = existing.getQuery();
        for (Field primaryField : fields) {
            String columnName = existing.getTableStructure().getColumnName(primaryField);
            try {
                Object object = modelContainer.getValue(primaryField.getName());
                if (object == null) {
                    throw new PrimaryKeyCannotBeNullException("The primary key: " + primaryField.getName()
                            + "from " + existing.getTableStructure().getTableName() + " cannot be null.");
                } else {
                    query = query.replaceFirst("\\?", existing.convertValueToString(columnName, object));
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return query;
    }
}
