package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: Defines an INDEX in Sqlite. It basically speeds up data retrieval over large datasets.
 * It gets generated from {@link Table#indexGroups()}, but also can be manually constructed. These are activated
 * and deactivated manually.
 */
public class IndexProperty<T extends Model> {

    private final List<IProperty> propertyList = new ArrayList<>();

    private final Class<T> table;
    private final boolean unique;
    private String indexName;

    public IndexProperty(String indexName, boolean unique, Class<T> table, IProperty... properties) {
        this.indexName = indexName;
        Collections.addAll(propertyList, properties);
        this.table = table;
        this.unique = unique;
    }

    public void createIfNotExists() {
        FlowManager.getDatabaseForTable(table).getWritableDatabase().execSQL(getCreateQuery());
    }

    public void drop() {
        FlowManager.getDatabaseForTable(table).getWritableDatabase().execSQL(getDropQuery());
    }

    public String getCreateQuery() {
        return new StringBuilder("CREATE ").append(unique ? "UNIQUE " : "").append("INDEX IF NOT EXISTS ")
                .append(getIndexName())
                .append(" ON ")
                .append(FlowManager.getTableName(table))
                .append("(")
                .append(QueryBuilder.join(",", propertyList))
                .append(")").toString();
    }

    public String getDropQuery() {
        return "DROP INDEX" + getIndexName();
    }

    public String getIndexName() {
        return QueryBuilder.quote(indexName);
    }
}
