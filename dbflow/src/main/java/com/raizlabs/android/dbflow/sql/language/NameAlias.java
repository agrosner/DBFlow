package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: Represents a column name as an alias to its original name. EX: SELECT `money` AS `myMoney`. However
 * if a an asName is not specified, then at its base this simply represents a column.
 */
public class NameAlias implements Query {

    private String name;

    private String aliasName;

    private boolean tickName = true;

    public NameAlias(String name) {
        this.name = name;
    }

    public NameAlias(String name, String aliasName) {
        this(name);
        as(aliasName);
    }

    public NameAlias(NameAlias existing) {
        this(existing.name, existing.aliasName);
    }

    public NameAlias as(String aliasName) {
        this.aliasName = aliasName;
        return this;
    }

    public NameAlias tickName(boolean tickName) {
        this.tickName = tickName;
        return this;
    }

    @Override
    public String getQuery() {
        return getAliasName();
    }

    @Override
    public String toString() {
        return getDefinition();
    }

    public String getDefinition() {
        StringBuilder definition = new StringBuilder(tickName ? getName() : getNameNoTicks());
        if (aliasName != null) {
            definition.append(" AS ").append(getAliasName());
        }
        return definition.toString();
    }

    public String getAliasName() {
        return QueryBuilder.quote(getAliasNameNoTicks());
    }

    public String getAliasNamePropertyNoTicks() {
        return aliasName;
    }

    public String getAliasNameNoTicks() {
        return aliasName != null ? aliasName : name;
    }

    public String getName() {
        return QueryBuilder.quote(name);
    }

    public String getNameNoTicks() {
        return name;
    }
}
