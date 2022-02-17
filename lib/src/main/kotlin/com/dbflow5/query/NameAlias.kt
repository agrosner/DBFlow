package com.dbflow5.query

import com.dbflow5.isNotNullOrEmpty
import com.dbflow5.query.operations.InferredObjectConverter
import com.dbflow5.query.operations.OpStart
import com.dbflow5.query.operations.Operation
import com.dbflow5.query.operations.Operator
import com.dbflow5.query.operations.SQLValueConverter
import com.dbflow5.query.operations.literalOf
import com.dbflow5.quoteIfNeeded
import com.dbflow5.sql.Query
import com.dbflow5.stripQuotes

/**
 * Description: Rewritten from the ground up, this class makes it easier to build an alias.
 */
class NameAlias(
    private val name: String,
    private val aliasName: String? = null,
    val tableName: String? = null,
    val keyword: String? = null,
    val shouldStripIdentifier: Boolean = true,
    val shouldStripAliasName: Boolean = true,
    private val shouldAddIdentifierToQuery: Boolean = true,
    private val shouldAddIdentifierToAliasName: Boolean = true
) : Query, OpStart<Any?> {

    /**
     * @return The name used in queries. If an alias is specified, use that, otherwise use the name
     * of the property with a table name (if specified).
     */
    override val query: String
        get() = when {
            aliasName.isNotNullOrEmpty() -> aliasName ?: ""
            name.isNotNullOrEmpty() -> fullName()
            else -> ""
        }

    override val nameAlias: NameAlias = this

    override val valueConverter: SQLValueConverter<Any?> = InferredObjectConverter

    /**
     * @return The value used as a key. Uses either the [.aliasNameRaw]
     * or the [.nameRaw], depending on what's specified.
     */
    val nameAsKey: String?
        get() = if (aliasName.isNotNullOrEmpty()) {
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
            if (aliasName.isNotNullOrEmpty()) {
                query += " AS ${aliasName()}"
            }
            if (keyword.isNotNullOrEmpty()) {
                query = "$keyword $query"
            }
            return query
        }

    private constructor(builder: Builder) : this(
        name = if (builder.shouldStripIdentifier) {
            builder.name.stripQuotes() ?: ""
        } else {
            builder.name
        },
        keyword = builder.keyword,
        aliasName = if (builder.shouldStripAliasName) {
            builder.aliasName.stripQuotes()
        } else {
            builder.aliasName
        },
        tableName = if (builder.tableName.isNotNullOrEmpty()) {
            builder.tableName.quoteIfNeeded()
        } else {
            null
        },
        shouldStripIdentifier = builder.shouldStripIdentifier,
        shouldStripAliasName = builder.shouldStripAliasName,
        shouldAddIdentifierToQuery = builder.shouldAddIdentifierToQuery,
        shouldAddIdentifierToAliasName = builder.shouldAddIdentifierToAliasName
    )

    /**
     * @return The real column name.
     */
    fun name(): String {
        return if (name.isNotNullOrEmpty() && shouldAddIdentifierToQuery)
            name.quoteIfNeeded()
        else name
    }

    /**
     * @return The name, stripped from identifier syntax completely.
     */
    fun nameRaw(): String = if (shouldStripIdentifier) name else name.stripQuotes() ?: ""

    /**
     * @return The name used as part of the AS query.
     */
    fun aliasName(): String? {
        return if (aliasName.isNotNullOrEmpty() && shouldAddIdentifierToAliasName)
            aliasName.quoteIfNeeded()
        else aliasName
    }

    /**
     * @return The alias name, stripped from identifier syntax completely.
     */
    fun aliasNameRaw(): String? =
        if (shouldStripAliasName) aliasName else aliasName.stripQuotes()

    /**
     * @return The `{tableName}`.`{name}`. If [.tableName] specified.
     */
    fun fullName(): String =
        (if (tableName.isNotNullOrEmpty()) "$tableName." else "") + name()

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
        fun joinNames(operation: String, vararg names: String): NameAlias =
            rawBuilder(names.joinToString(" $operation ")).build()

        /**
         * Combines any number of names into a single [NameAlias] separated by some operation.
         *
         * @param operation The operation to separate into.
         * @param names     The names to join.
         * @return The new namealias object.
         */
        fun joinNames(operation: Operation, vararg names: String) =
            joinNames(operation.value, *names)

        fun builder(name: String): Builder = Builder(name)

        fun tableNameBuilder(tableName: String): Builder = Builder("")
            .withTable(tableName)

        /**
         * @param name The raw name of this alias.
         * @return A new instance without adding identifier `` to any part of the query.
         */
        fun rawBuilder(name: String): Builder {
            return Builder(name)
                .shouldStripIdentifier(false)
                .shouldAddIdentifierToName(false)
        }

        fun of(name: String): NameAlias = builder(name).build()

        fun of(name: String, aliasName: String): NameAlias =
            builder(name).`as`(aliasName).build()

        fun ofTable(tableName: String, name: String): NameAlias =
            builder(name).withTable(tableName).build()
    }

    override fun `as`(name: String, shouldAddIdentifierToAlias: Boolean): Operator<Any?> =
        literalOf(
            newBuilder()
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
                .`as`(name)
                .build()
        )
}

val String.nameAlias
    get() = NameAlias.of(this)

fun String.`as`(alias: String = "") = NameAlias.of(this, alias)
