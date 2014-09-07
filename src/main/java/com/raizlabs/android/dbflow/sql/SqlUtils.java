package com.raizlabs.android.dbflow.sql;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class SqlUtils {

    /**
     * Queries the DB for a {@link android.database.Cursor} and converts it into a list.
     * @param modelClass The class to construct the data from the DB into
     * @param sql The SQL command to perform, must not be ; terminated.
     * @param args  You may include ?s in where clause in the query,
     *     which will be replaced by the values from selectionArgs. The
     *     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return a list of {@link ModelClass}
     */
    public static <ModelClass extends Model> List<ModelClass> queryList(Class<ModelClass> modelClass, String sql, String...args) {
        Cursor cursor = FlowManager.getWritableDatabase().rawQuery(sql, args);
        List<ModelClass> list = convertToList(modelClass, cursor);
        cursor.close();
        return list;
    }

    /**
     * Queries the DB and returns the first {@link com.raizlabs.android.dbflow.structure.Model} it finds. Note:
     * this may return more than one object, but only will return the first item in the list.
     * @param modelClass The class to construct the data from the DB into
     * @param sql The SQL command to perform, must not be ; terminated.
     * @param args  You may include ?s in where clause in the query,
     *     which will be replaced by the values from selectionArgs. The
     *     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return a single {@link ModelClass}
     */
    public static <ModelClass extends Model> ModelClass querySingle(Class<ModelClass> modelClass, String sql, String...args) {
        List<ModelClass> list = queryList(modelClass, sql, args);

        ModelClass retModel = null;
        if(list.size() > 0) {
            retModel = list.get(0);
        }
        return retModel;
    }

    /**
     * Loops through a cursor and builds a list of {@link ModelClass} objects.
     * @param table The model class that we convert the cursor data into.
     * @param cursor The cursor from the DB
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

        }
        catch (IllegalArgumentException i){
            throw new RuntimeException("Default constructor for: " + table.getName() + " was not found.");
        } catch (Exception e) {
            FlowLog.e(SqlUtils.class.getSimpleName(), "Failed to process cursor.", e);
        }

        return entities;
    }
}
