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

    boolean shouldStripTicks;

    /**
     * Combines any number of names into a single {@link NameAlias} separated by some operation.
     *
     * @param operation The operation to separate into.
     * @param names     The names to join.
     * @return The new namealias object.
     */
    public static NameAlias joinNames(String operation, String... names) {
        if (names.length == 0) {
            return null;
        }
        String newName = "";
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                newName += operation + " ";
            }
            newName += names[i] + " ";
        }
        return new NameAlias(newName, false);
    }

    /**
     * Internal usage only. We don't strip out ticks in a name ot preserve its compound name state.
     *
     * @param name             The name of this {@link NameAlias}.
     * @param shouldStripTicks True we strip ticks from name, false we preserve them.
     */
    public NameAlias(@NonNull String name, boolean shouldStripTicks) {
        this.shouldStripTicks = shouldStripTicks;
        if (shouldStripTicks) {
            this.name = QueryBuilder.stripQuotes(name);
        } else {
            this.name = name;
        }
    }

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
        tickName(existing.shouldTickName());
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
        if (hasAlias()) {
            definition.append(" AS ").append(getAliasName());
        }
        return definition.toString();
    }

    /**
     * @return True if this has an actual alias.
     */
    public boolean hasAlias() {
        return aliasName != null;
    }

    /**
     * @return The alias name of this table. If none is defined, it returns {@link #getName()}.
     */
    @NonNull
    public String getAliasName() {
        return aliasName != null ? QueryBuilder.quote(getAliasNameRaw()) : getName();
    }

    public boolean shouldStripTicks() {
        return shouldStripTicks;
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

    public boolean shouldTickName() {
        return tickName;
    }

    /**
     * @return The original name of this alias.
     */
    @NonNull
    public String getName() {
        String fullName = "";
        if (prefixName != null) {
            fullName += (tickName ? QueryBuilder.quoteIfNeeded(prefixName) : prefixName) + ".";
        }
        fullName += (tickName ? QueryBuilder.quote(name) : getNamePropertyRaw());
        return fullName;
    }

    /**
     * @return The name of this alias.
     */
    @NonNull
    public String getNamePropertyRaw() {
        return name;
    }
}
