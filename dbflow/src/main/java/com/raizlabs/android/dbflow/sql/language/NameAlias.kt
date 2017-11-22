package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.StringUtils
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.QueryBuilder

/**
 * Description: Rewritten from the ground up, this class makes it easier to build an alias.
 */
class NameAlias(private val name: String,
                private val aliasName: String? = null,
                private val tableName: String? = null,
                private val keyword: String? = null,
                private val shouldStripIdentifier: Boolean = true,
                private val shouldStripAliasName: Boolean = true,
                private val shouldAddIdentifierToQuery: Boolean = true,
                private val shouldAddIdentifierToAliasName: Boolean = true) : Query {

    /**
     * @return The name used in queries. If an alias is specified, use that, otherwise use the name
     * of the property with a table name (if specified).
     */
    override val query: String
        get() = when {
            StringUtils.isNotNullOrEmpty(aliasName) -> aliasName!!
            StringUtils.isNotNullOrEmpty(name) -> fullName()
            else -> ""
        }

    /**
     * @return The value used as a key. Uses either the [.aliasNameRaw]
     * or the [.nameRaw], depending on what's specified.
     */
    val nameAsKey: String?
        get() = if (StringUtils.isNotNullOrEmpty(aliasName)) {
            aliasNameRaw()
        } else {
            nameRaw()
        }

    /**
     * @return The full query that represents itself with `{tableName}`.`{name}` AS `{aliasName}`
     */
    val fullQuery: String
        get() {
            var query = fullName()
            if (StringUtils.isNotNullOrEmpty(aliasName)) {
                query += " AS ${aliasName()}"
            }
            if (StringUtils.isNotNullOrEmpty(keyword)) {
                query = "$keyword $query"
            }
            return query
        }

    private constructor(builder: Builder) : this(
            name = if (builder.shouldStripIdentifier) {
                QueryBuilder.stripQuotes(builder.name)
            } else {
                builder.name
            },
            keyword = builder.keyword,
            aliasName = if (builder.shouldStripAliasName) {
                QueryBuilder.stripQuotes(builder.aliasName)
            } else {
                builder.aliasName
            },
            tableName = if (StringUtils.isNotNullOrEmpty(builder.tableName)) {
                QueryBuilder.quoteIfNeeded(builder.tableName)
            } else {
                null
            },
            shouldStripIdentifier = builder.shouldStripIdentifier,
            shouldStripAliasName = builder.shouldStripAliasName,
            shouldAddIdentifierToQuery = builder.shouldAddIdentifierToQuery,
            shouldAddIdentifierToAliasName = builder.shouldAddIdentifierToAliasName)

    /**
     * @return The real column name.
     */
    fun name(): String? {
        return if (StringUtils.isNotNullOrEmpty(name) && shouldAddIdentifierToQuery)
            QueryBuilder.quoteIfNeeded(name)
        else name
    }

    /**
     * @return The name, stripped from identifier syntax completely.
     */
    fun nameRaw(): String = if (shouldStripIdentifier) name else QueryBuilder.stripQuotes(name)

    /**
     * @return The name used as part of the AS query.
     */
    fun aliasName(): String? {
        return if (StringUtils.isNotNullOrEmpty(aliasName) && shouldAddIdentifierToAliasName)
            QueryBuilder.quoteIfNeeded(aliasName)
        else aliasName
    }

    /**
     * @return The alias name, stripped from identifier syntax completely.
     */
    fun aliasNameRaw(): String? =
            if (shouldStripAliasName) aliasName else QueryBuilder.stripQuotes(aliasName)

    /**
     * @return The `{tableName}`.`{name}`. If [.tableName] specified.
     */
    fun fullName(): String =
            (if (StringUtils.isNotNullOrEmpty(tableName)) "$tableName." else "") + name

    override fun toString(): String = fullQuery

    /**
     * @return Constructs a builder as a new instance that can be modified without fear.
     */
    fun newBuilder(): Builder {
        return Builder(name)
                .keyword(keyword)
                .`as`(aliasName)
                .shouldStripAliasName(shouldStripAliasName)
                .shouldStripIdentifier(shouldStripIdentifier)
                .shouldAddIdentifierToName(shouldAddIdentifierToQuery)
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAliasName)
                .withTable(tableName)
    }


    class Builder(internal val name: String) {
        internal var aliasName: String? = null
        internal var tableName: String? = null
        internal var shouldStripIdentifier = true
        internal var shouldStripAliasName = true
        internal var shouldAddIdentifierToQuery = true
        internal var shouldAddIdentifierToAliasName = true
        internal var keyword: String? = null

        /**
         * Appends a DISTINCT that prefixes this alias class.
         */
        fun distinct(): Builder = keyword("DISTINCT")

        /**
         * Appends a keyword that prefixes this alias class.
         */
        fun keyword(keyword: String?) = apply {
            this.keyword = keyword
        }

        /**
         * Provide an alias that is used `{name}` AS `{aliasName}`
         */
        fun `as`(aliasName: String?) = apply {
            this.aliasName = aliasName
        }

        /**
         * Provide a table-name prefix as such: `{tableName}`.`{name}`
         */
        fun withTable(tableName: String?) = apply {
            this.tableName = tableName
        }

        /**
         * @param shouldStripIdentifier If true, we normalize the identifier [.name] from any
         * ticks around the name. If false, we leave it as such.
         */
        fun shouldStripIdentifier(shouldStripIdentifier: Boolean) = apply {
            this.shouldStripIdentifier = shouldStripIdentifier
        }

        /**
         * @param shouldStripAliasName If true, we normalize the identifier [.aliasName] from any
         * ticks around the name. If false, we leave it as such.
         */
        fun shouldStripAliasName(shouldStripAliasName: Boolean) = apply {
            this.shouldStripAliasName = shouldStripAliasName
        }

        /**
         * @param shouldAddIdentifierToName If true (default), we add the identifier to the name: `{name}`
         */
        fun shouldAddIdentifierToName(shouldAddIdentifierToName: Boolean) = apply {
            this.shouldAddIdentifierToQuery = shouldAddIdentifierToName
        }

        /**
         * @param shouldAddIdentifierToAliasName If true (default), we add an identifier to the alias
         * name. `{aliasName}`
         */
        fun shouldAddIdentifierToAliasName(shouldAddIdentifierToAliasName: Boolean) = apply {
            this.shouldAddIdentifierToAliasName = shouldAddIdentifierToAliasName
        }

        fun build(): NameAlias = NameAlias(this)

    }

    companion object {

        /**
         * Combines any number of names into a single [NameAlias] separated by some operation.
         *
         * @param operation The operation to separate into.
         * @param names     The names to join.
         * @return The new namealias object.
         */
        fun joinNames(operation: String, vararg names: String): NameAlias {
            var newName = ""
            for (i in names.indices) {
                if (i > 0) {
                    newName += " $operation "
                }
                newName += names[i]
            }
            return rawBuilder(newName).build()
        }

        fun builder(name: String): Builder = Builder(name)

        /**
         * @param name The raw name of this alias.
         * @return A new instance without adding identifier `` to any part of the query.
         */
        fun rawBuilder(name: String): Builder {
            return Builder(name)
                    .shouldStripIdentifier(false)
                    .shouldAddIdentifierToName(false)
        }

        fun of(name: String): NameAlias = NameAlias.builder(name).build()

        fun of(name: String, aliasName: String): NameAlias =
                NameAlias.builder(name).`as`(aliasName).build()

        fun ofTable(tableName: String, name: String): NameAlias =
                NameAlias.builder(name).withTable(tableName).build()
    }
}
