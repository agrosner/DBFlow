package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: Rewritten from the ground up, this class makes it easier to build an alias.
 */
public class NameAlias2 implements Query {

    private final String name;
    private final String aliasName;
    private final String tableName;
    private final boolean shouldStripIdentifier;

    public NameAlias2(Builder builder) {
        if (builder.shouldStripIdentifier) {
            name = QueryBuilder.stripQuotes(builder.name);
        } else {
            name = builder.name;
        }
        aliasName = builder.aliasName;
        tableName = builder.tableName;
        shouldStripIdentifier = builder.shouldStripIdentifier;
    }

    /**
     * @return The real column name.
     */
    public String name() {
        return name;
    }

    /**
     * @return The name used as part of the AS query.
     */
    public String aliasName() {
        return aliasName;
    }

    /**
     * @return the table name of this query, if specified.
     */
    public String tableName() {
        return tableName;
    }

    /**
     * @return true if the name was stripped from identifier, false if not.
     */
    public boolean shouldStripIdentifier() {
        return shouldStripIdentifier;
    }

    /**
     * @return The `{tableName}`.`{name}`.
     */
    public String fullName() {
        return tableName() + "." + name();
    }

    /**
     * @return The name used in queries. If an alias is specified, use that, otherwise use the name
     * of the property with a table name (if specified).
     */
    @Override
    public String getQuery() {
        if (StringUtils.isNotNullOrEmpty(aliasName())) {
            return fullName();
        } else {
            return aliasName();
        }
    }


    public static class Builder {

        private final String name;
        private String aliasName;
        private String tableName;
        private boolean shouldStripIdentifier;

        public Builder(String name) {
            this.name = name;
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

        public NameAlias2 build() {
            return new NameAlias2(this);
        }

    }
}
