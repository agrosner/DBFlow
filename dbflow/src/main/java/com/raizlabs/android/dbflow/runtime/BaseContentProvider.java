package com.raizlabs.android.dbflow.runtime;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.DatabaseHolder;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: The base provider class that {@link com.raizlabs.android.dbflow.annotation.provider.ContentProvider}
 * extend when generated.
 */
public abstract class BaseContentProvider extends ContentProvider {

    protected Class<? extends DatabaseHolder> moduleClass;

    protected final Map<Class<? extends Model>, PropertyConverter> propertyConverterMap = new HashMap<>();

    /**
     * Converts the column into a {@link Property}. This exists since the property method is static and cannot
     * be referenced easily.
     */
    public interface PropertyConverter {
        IProperty fromName(String columnName);
    }

    protected BaseContentProvider() {

    }

    protected BaseContentProvider(Class<? extends DatabaseHolder> databaseHolderClass) {
        this.moduleClass = databaseHolderClass;
    }

    /**
     * Converts a projection of {@link String} column names into an array of properties. Any columns
     * not found may throw an {@link IllegalArgumentException}. This helps to prevent SQL injection attacks by
     * explicitly checking for correct columns.
     *
     * @param propertyConverter The converter to convert the name.
     * @param projection        The projection to convert.
     * @return An array of {@link IProperty}.
     */
    protected static IProperty[] toProperties(PropertyConverter propertyConverter, String... projection) {
        IProperty[] properties = new IProperty[projection.length];
        for (int i = 0; i < projection.length; i++) {
            String columnName = projection[i];
            properties[i] = propertyConverter.fromName(columnName);
        }
        return properties;
    }

    protected static SQLCondition[] toConditions(String selection, String[] selectionArgs) {
        List<SQLCondition> conditions = new ArrayList<>();
        if (StringUtils.isNotNullOrEmpty(selection)) {
            String[] stringConditions = selection.split(" AND ");
            if (selectionArgs != null && selectionArgs.length > 0 && selectionArgs.length > stringConditions.length) {
                throw new IllegalArgumentException("Too many bind arguments.  "
                    + selectionArgs.length + " arguments were provided but the selection query needs "
                    + stringConditions.length + " arguments.");
            }
            List<String> copySelectionArgs = selectionArgs != null ? new ArrayList<>(Arrays.asList(selectionArgs)) : new ArrayList<String>();
            for (int i = 0; i < stringConditions.length; i++) {
                String stringCondition = stringConditions[i];
                if (stringCondition.endsWith("?")) {
                    stringConditions[i] = stringCondition.substring(0, stringCondition.length() - 1) + copySelectionArgs.remove(0);
                }

                String[] params = stringCondition.split("=");
                if (params.length == 0) {
                    throw new IllegalArgumentException("Selection conditions must be of Operation Type.");
                } else if (params.length == 2) {
                    conditions.add(Condition.column(new NameAlias(params[0])).eq(params[1]));
                } else {
                    throw new IllegalStateException("Something went wrong. Condition could not be associated with equals");
                }
            }
        }

        return conditions.toArray(new SQLCondition[conditions.size()]);
    }

    protected static List<OrderBy> toOrderBy(String sort, PropertyConverter propertyConverter) {
        List<OrderBy> orderBies = new ArrayList<>();
        if (StringUtils.isNotNullOrEmpty(sort)) {
            String[] sortArray = sort.split(",");
            for (String s : sortArray) {
                String columnName;
                String ordering;
                if (s.endsWith(OrderBy.ASCENDING)) {
                    ordering = OrderBy.ASCENDING;
                    columnName = s.replace(OrderBy.ASCENDING, "");
                } else if (s.endsWith(OrderBy.DESCENDING)) {
                    ordering = OrderBy.DESCENDING;
                    columnName = s.replace(OrderBy.DESCENDING, "");
                } else {
                    // default SQLite is ascending order, we will crash if the s is not a valid column name.
                    ordering = OrderBy.ASCENDING;
                    columnName = s;
                }
                OrderBy orderBy = OrderBy.fromProperty(propertyConverter.fromName(columnName));
                if (ordering.equals(OrderBy.ASCENDING)) {
                    orderBy.ascending();
                } else {
                    orderBy.descending();
                }
                orderBies.add(orderBy);
            }
        }
        return orderBies;
    }

    protected BaseDatabaseDefinition database;

    @Override
    public boolean onCreate() {
        // If this is a module, then we need to initialize the module as part
        // of the creation process. We can assume the framework has been general
        // framework has been initialized.
        if (moduleClass != null) {
            FlowManager.initModule(moduleClass);
        }

        return true;
    }

    @Override
    public int bulkInsert(@NonNull final Uri uri, @NonNull final ContentValues[] values) {
        final int[] count = {0};
        TransactionManager.transact(getDatabase().getWritableDatabase(), new Runnable() {
            @Override
            public void run() {
                for (ContentValues contentValues : values) {
                    count[0] += bulkInsert(uri, contentValues);
                }
            }
        });
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return count[0];
    }

    protected abstract String getDatabaseName();

    protected abstract int bulkInsert(Uri uri, ContentValues contentValues);

    protected BaseDatabaseDefinition getDatabase() {
        if (database == null) {
            database = FlowManager.getDatabase(getDatabaseName());
        }
        return database;
    }

}
