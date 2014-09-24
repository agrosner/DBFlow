package com.grosner.dbflow.structure.json;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.BaseTransaction;
import com.grosner.dbflow.runtime.transaction.process.DeleteModelListTransaction;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.builder.PrimaryKeyCannotBeNullException;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.ColumnType;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.StructureUtils;
import com.grosner.dbflow.structure.TableStructure;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class JsonStructureUtils {

    /**
     * Saves a {@link com.grosner.dbflow.structure.json.JSONModel} to the database.
     *
     * @param jsonModel    The json model to save
     * @param async        Where it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately/
     * @param mode         The save mode, can be {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_DEFAULT},
     *                     {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_INSERT}, {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_UPDATE}
     * @param notify       If we should notify all of our {@link com.grosner.dbflow.runtime.observer.ModelObserver}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public static <ModelClass extends Model> void save(JSONModel<ModelClass> jsonModel, boolean async, int mode, boolean notify) {
        if(!async) {

            TableStructure<ModelClass> tableStructure = jsonModel.mTableStructure;

            Class<ModelClass> table = tableStructure.getModelType();
            ConditionQueryBuilder<ModelClass> primaryConditionQueryBuilder = tableStructure.getManager()
                    .getStructure().getPrimaryWhereQuery(jsonModel.getTable());

            final SQLiteDatabase db = jsonModel.mManager.getWritableDatabase();
            final ContentValues values = new ContentValues();

            Iterator<String> jsonKeys = jsonModel.mJson.keys();
            while (jsonKeys.hasNext()){
                String jsonKey = jsonKeys.next();
                Object value = jsonModel.mJson.opt(jsonKey);

                Field field = tableStructure.getField(jsonKey);
                field.setAccessible(true);
                SqlUtils.putField(values, jsonModel.mManager, field, tableStructure.getColumnName(field), value);
            }

            boolean exists = false;
            if (mode == SqlUtils.SAVE_MODE_DEFAULT) {
                exists = exists(jsonModel);
            } else if (mode == SqlUtils.SAVE_MODE_UPDATE) {
                exists = true;
            }

            if (exists) {
                exists = (db.update(tableStructure.getTableName(), values,
                        getModelBackedWhere(primaryConditionQueryBuilder, tableStructure.getPrimaryKeys(), jsonModel), null) != 0);
            }

            if (!exists) {
                long id = db.insert(tableStructure.getTableName(), null, values);

                Collection<Field> fields = tableStructure.getPrimaryKeys();
                for (Field field : fields) {
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
                // will convert the json to a model here
                jsonModel.mManager.getStructure().fireModelChanged(jsonModel.toModel());
            }

        } else {
            TransactionManager.getInstance().save(DBTransactionInfo.create(), jsonModel);
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
    public static <ModelClass extends Model> void delete(final JSONModel<ModelClass> jsonModel, boolean async) {
        if (!async) {
            ConditionQueryBuilder<ModelClass> conditionQueryBuilder = jsonModel.mManager.getStructure().getPrimaryWhereQuery(jsonModel.getTable());
            jsonModel.mManager.getWritableDatabase().delete(jsonModel.mTableStructure.getTableName(), getPrimaryModelWhere(conditionQueryBuilder, jsonModel), null);
        } else {
            TransactionManager.getInstance().delete(DBTransactionInfo.create(), jsonModel);
        }
    }

    /**
     * Returns whether the given {@link com.grosner.dbflow.structure.json.JSONModel} exists in the DB as the {@link ModelClass}
     * table it points to.
     * @param jsonModel The json model that points to a {@link com.grosner.dbflow.structure.Model} class
     * @param <ModelClass> The class that implements {@link ModelClass}
     * @return If the model that the {@link com.grosner.dbflow.structure.json.JSONModel} points to exists in the DB
     */
    private static <ModelClass extends Model> boolean exists(JSONModel<ModelClass> jsonModel) {
        ConditionQueryBuilder<ModelClass> primaryCondition = jsonModel.mManager.getStructure().getPrimaryWhereQuery(jsonModel.getTable());
        return new Select(jsonModel.mManager).from(jsonModel.getTable())
                .where(getPrimaryModelWhere(primaryCondition, jsonModel)).hasData();
    }

    /**
     * Builds a {@link com.grosner.dbflow.structure.Model} where query with its primary keys. The existing must
     * be based off the primary keys of the model.
     *
     * @param existing The existing where query we wish to generate new one from the model.
     * @param model    The existing model with all of its primary keys filled in
     * @return
     */
    public static <ModelClass extends Model> String getPrimaryModelWhere(ConditionQueryBuilder<ModelClass> existing, JSONModel<ModelClass> model) {
        return getModelBackedWhere(existing, existing.getTableStructure().getPrimaryKeys(), model);
    }

    /**
     * Returns a where query String from the existing builder and collection of fields. This is only different
     * than {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder#getModelBackedWhere(com.grosner.dbflow.sql.builder.ConditionQueryBuilder, java.util.Collection, com.grosner.dbflow.structure.Model)}
     * in that we call {@link org.json.JSONObject#get(String)} for the value of the field.
     *
     * @param existing     The existing where query we wish to generate new one from the model.
     * @param fields       The list of fields that we look up the column names for
     * @param model        The {@link com.grosner.dbflow.structure.json.JSONModel} to get the field values from
     * @param <ModelClass>
     * @return
     */
    public static <ModelClass extends Model> String getModelBackedWhere(ConditionQueryBuilder<ModelClass> existing,
                                             Collection<Field> fields, JSONModel<ModelClass> model) {
        String query = existing.getQuery();
        for (Field primaryField : fields) {
            String columnName = existing.getTableStructure().getColumnName(primaryField);
            try {
                Object object = model.mJson.opt(primaryField.getName());
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
