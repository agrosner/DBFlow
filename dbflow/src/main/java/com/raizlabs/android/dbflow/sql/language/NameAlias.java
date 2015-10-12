package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    private String prefixName;

    public NameAlias(@NonNull String name) {
        this.name = QueryBuilder.stripQuotes(name);
    }

    public NameAlias(@NonNull String name, @NonNull String aliasName) {
        this(name);
        as(aliasName);
    }

    /**
     * Copy constructor.
     *
     * @param existing
     */
    public NameAlias(@NonNull NameAlias existing) {
        this(existing.name, existing.aliasName);
    }

    public NameAlias as(@NonNull String aliasName) {
        this.aliasName = QueryBuilder.stripQuotes(aliasName);
        return this;
    }

    public NameAlias withTable(@NonNull String prefixName) {
        this.prefixName = QueryBuilder.stripQuotes(prefixName);
        return this;
    }

    /**
     * @param shouldTickName if true the names are quoted. False we leave out the quotes.
     * @return This instance.
     */
    public NameAlias tickName(boolean shouldTickName) {
        this.tickName = shouldTickName;
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

    /**
     * @return The full definition name that this Alias uses to define its definition.
     * E.g: `firstName` AS `FN`.
     */
    @NonNull
    public String getDefinition() {
        StringBuilder definition = new StringBuilder();
        if (prefixName != null) {
            definition.append(tickName ? QueryBuilder.quoteIfNeeded(prefixName) : prefixName)
                    .append(".");
        }
        definition.append(tickName ? getName() : getNamePropertyRaw());
        if (aliasName != null) {
            definition.append(" AS ").append(getAliasName());
        }
        return definition.toString();
    }

    /**
     * @return The alias name of this table. If none is defined, it returns {@link #getName()}.
     */
    @NonNull
    public String getAliasName() {
        return QueryBuilder.quote(getAliasNameRaw());
    }

    /**
     * @return The value of the aliasName. It may be null.
     */
    @Nullable
    public String getAliasPropertyRaw() {
        return aliasName;
    }

    /**
     * @return The alias name for this table without any quotes. If none is defined it returns {@link #getNamePropertyRaw()}.
     */
    public String getAliasNameRaw() {
        return aliasName != null ? aliasName : name;
    }

    /**
     * @return The original name of this alias.
     */
    @NonNull
    public String getName() {
        return QueryBuilder.quote(name);
    }

    /**
     * @return The name of this alias.
     */
    @NonNull
    public String getNamePropertyRaw() {
        return name;
    }
}
