package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;

/**
 * Description: Rewritten from the ground up, this class makes it easier to build an alias.
 */
public class NameAlias implements Query {

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
                newName += " " + operation + " ";
            }
            newName += names[i];
        }
        return rawBuilder(newName).build();
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * @param name The raw name of this alias.
     * @return A new instance without adding identifier `` to any part of the query.
     */
    public static Builder rawBuilder(String name) {
        return new Builder(name)
                .shouldStripIdentifier(false)
                .shouldAddIdentifierToName(false);
    }

    private final String name;
    private final String aliasName;
    private final String tableName;
    private final String keyword;
    private final boolean shouldStripIdentifier;
    private final boolean shouldStripAliasName;
    private final boolean shouldAddIdentifierToQuery;
    private final boolean shouldAddIdentifierToAliasName;

    private NameAlias(Builder builder) {
        if (builder.shouldStripIdentifier) {
            name = QueryBuilder.stripQuotes(builder.name);
        } else {
            name = builder.name;
        }
        keyword = builder.keyword;
        if (builder.shouldStripAliasName) {
            aliasName = QueryBuilder.stripQuotes(builder.aliasName);
        } else {
            aliasName = builder.aliasName;
        }
        if (StringUtils.isNotNullOrEmpty(builder.tableName)) {
            tableName = QueryBuilder.quoteIfNeeded(builder.tableName);
        } else {
            tableName = null;
        }
        shouldStripIdentifier = builder.shouldStripIdentifier;
        shouldStripAliasName = builder.shouldStripAliasName;
        shouldAddIdentifierToQuery = builder.shouldAddIdentifierToQuery;
        shouldAddIdentifierToAliasName = builder.shouldAddIdentifierToAliasName;
    }

    /**
     * @return The real column name.
     */
    public String name() {
        return (StringUtils.isNotNullOrEmpty(name) && shouldAddIdentifierToQuery) ?
                QueryBuilder.quoteIfNeeded(name) : name;
    }

    /**
     * @return The name, stripped from identifier syntax completely.
     */
    public String nameRaw() {
        return shouldStripIdentifier ? name : QueryBuilder.stripQuotes(name);
    }

    /**
     * @return The name used as part of the AS query.
     */
    public String aliasName() {
        return (StringUtils.isNotNullOrEmpty(aliasName) && shouldAddIdentifierToAliasName) ?
                QueryBuilder.quoteIfNeeded(aliasName) : aliasName;
    }

    /**
     * @return The alias name, stripped from identifier syntax completely.
     */
    public String aliasNameRaw() {
        return shouldStripAliasName ? aliasName : QueryBuilder.stripQuotes(aliasName);
    }

    /**
     * @return the table name of this query, if specified.
     */
    public String tableName() {
        return tableName;
    }

    /**
     * @return The keyword that prefixes this alias.
     */
    public String keyword() {
        return keyword;
    }

    /**
     * @return true if the name was stripped from identifier, false if not.
     */
    public boolean shouldStripIdentifier() {
        return shouldStripIdentifier;
    }

    /**
     * @return true if the alias was stripped from identifier, false if not.
     */
    public boolean shouldStripAliasName() {
        return shouldStripAliasName;
    }

    /**
     * @return The `{tableName}`.`{name}`. If {@link #tableName()} specified.
     */
    public String fullName() {
        return (StringUtils.isNotNullOrEmpty(tableName) ? (tableName() + ".") : "") + name();
    }

    /**
     * @return The name used in queries. If an alias is specified, use that, otherwise use the name
     * of the property with a table name (if specified).
     */
    @Override
    public String getQuery() {
        if (StringUtils.isNotNullOrEmpty(aliasName)) {
            return aliasName();
        } else if (StringUtils.isNotNullOrEmpty(name)) {
            return fullName();
        } else {
            return "";
        }
    }

    /**
     * @return The value used as a key in a {@link ModelContainer}. Uses either the {@link #aliasNameRaw()}
     * or the {@link #nameRaw()}, depending on what's specified.
     */
    public String getNameAsKey() {
        if (StringUtils.isNotNullOrEmpty(aliasName)) {
            return aliasNameRaw();
        } else {
            return nameRaw();
        }
    }

    @Override
    public String toString() {
        return getFullQuery();
    }

    /**
     * @return The full query that represents itself with `{tableName}`.`{name}` AS `{aliasName}`
     */
    public String getFullQuery() {
        String query = fullName();
        if (StringUtils.isNotNullOrEmpty(aliasName)) {
            query += " AS " + aliasName();
        }
        if (StringUtils.isNotNullOrEmpty(keyword)) {
            query = keyword + " " + query;
        }
        return query;
    }

    /**
     * @return Constructs a builder as a new instance that can be modified without fear.
     */
    public Builder newBuilder() {
        return new Builder(name)
                .keyword(keyword)
                .as(aliasName)
                .shouldStripAliasName(shouldStripAliasName)
                .shouldStripIdentifier(shouldStripIdentifier)
                .shouldAddIdentifierToName(shouldAddIdentifierToQuery)
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAliasName)
                .withTable(tableName);
    }


    public static class Builder {

        private final String name;
        private String aliasName;
        private String tableName;
        private boolean shouldStripIdentifier = true;
        private boolean shouldStripAliasName = true;
        private boolean shouldAddIdentifierToQuery = true;
        private boolean shouldAddIdentifierToAliasName = true;
        private String keyword;

        public Builder(String name) {
            this.name = name;
        }

        /**
         * Appends a DISTINCT that prefixes this alias class.
         */
        public Builder distinct() {
            return keyword("DISTINCT");
        }

        /**
         * Appends a keyword that prefixes this alias class.
         */
        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        /**
         * Provide an alias that is used `{name}` AS `{aliasName}`
         */
        public Builder as(String aliasName) {
            this.aliasName = aliasName;
            return this;
        }

        /**
         * Provide a table-name prefix as such: `{tableName}`.`{name}`
         */
        public Builder withTable(String tableName) {
            this.tableName = tableName;
            return this;
        }

        /**
         * @param shouldStripIdentifier If true, we normalize the identifier {@link #name} from any
         *                              ticks around the name. If false, we leave it as such.
         */
        public Builder shouldStripIdentifier(boolean shouldStripIdentifier) {
            this.shouldStripIdentifier = shouldStripIdentifier;
            return this;
        }

        /**
         * @param shouldStripAliasName If true, we normalize the identifier {@link #aliasName} from any
         *                             ticks around the name. If false, we leave it as such.
         */
        public Builder shouldStripAliasName(boolean shouldStripAliasName) {
            this.shouldStripAliasName = shouldStripAliasName;
            return this;
        }

        /**
         * @param shouldAddIdentifierToName If true (default), we add the identifier to the name: `{name}`
         */
        public Builder shouldAddIdentifierToName(boolean shouldAddIdentifierToName) {
            this.shouldAddIdentifierToQuery = shouldAddIdentifierToName;
            return this;
        }

        /**
         * @param shouldAddIdentifierToAliasName If true (default), we add an identifier to the alias
         *                                       name. `{aliasName}`
         */
        public Builder shouldAddIdentifierToAliasName(boolean shouldAddIdentifierToAliasName) {
            this.shouldAddIdentifierToAliasName = shouldAddIdentifierToAliasName;
            return this;
        }

        public NameAlias build() {
            return new NameAlias(this);
        }

    }
}
