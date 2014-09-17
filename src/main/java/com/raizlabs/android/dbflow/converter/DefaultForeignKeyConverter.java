package com.raizlabs.android.dbflow.converter;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.TableStructure;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This is the default implementation for {@link com.raizlabs.android.dbflow.converter.ForeignKeyConverter}
 * . It will use comma separated values for all of the primary keys in the {@link com.raizlabs.android.dbflow.structure.Model} class.
 */
public class DefaultForeignKeyConverter implements ForeignKeyConverter {

    private static DefaultForeignKeyConverter converter;

    public static DefaultForeignKeyConverter getSharedConverter() {
        if (converter == null) {
            converter = new DefaultForeignKeyConverter();
        }
        return converter;
    }

    @Override
    public Class getModelClass() {
        return Class.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getDBValue(Model model) {
        TableStructure tableStructure = FlowManager.getTableStructureForClass(model.getClass());
        Collection<Field> primaries = tableStructure.getPrimaryKeys();
        QueryBuilder query = new QueryBuilder();
        int count = 0;
        for (Field field : primaries) {
            try {
                Object value = field.get(model);
                query.append(value.toString());

                if (count < primaries.size() - 1) {
                    query.append(",");
                }
            } catch (IllegalAccessException e) {
                FlowLog.logError(e);
            }
            count++;
        }
        return query.getQuery();
    }

    @Override
    public String[] getForeignKeys(String dbValue) {
        return dbValue != null ? dbValue.split(",") : new String[0];
    }
}
