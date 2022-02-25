package com.dbflow5.runtime

import android.net.Uri
import com.dbflow5.TABLE_QUERY_PARAM
import com.dbflow5.adapter2.DBRepresentable
import com.dbflow5.adapter2.ModelAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.query.NameAlias
import com.dbflow5.query.operations.BaseOperator
import com.dbflow5.query.operations.Operation
import com.dbflow5.query.operations.operator
import com.dbflow5.structure.ChangeAction

/**
 * Represents a notification used in [ContentResolverNotifier]
 */
sealed interface ContentNotification<Table : Any> {
    val uri: Uri
    val dbRepresentable: DBRepresentable<Table>
    val action: ChangeAction
    val authority: String

    fun createUri(fn: Uri.Builder.() -> Unit = {}): Uri = Uri.Builder().scheme("dbflow")
        .authority(authority)
        .appendQueryParameter(TABLE_QUERY_PARAM, dbRepresentable.name)
        .apply { if (action != ChangeAction.NONE) fragment(action.name) }
        .apply(fn)
        .build()

    data class TableChange<Table : Any>(
        override val action: ChangeAction,
        override val authority: String,
        override val dbRepresentable: DBRepresentable<Table>,
    ) : ContentNotification<Table> {
        override val uri: Uri by lazy { createUri() }
    }

    data class ModelChange<Table : Any>(
        override val dbRepresentable: DBRepresentable<Table>,
        override val action: ChangeAction,
        override val authority: String,
        val changedFields: List<BaseOperator.SingleValueOperator<*>>,
    ) : ContentNotification<Table> {
        constructor(
            model: Table,
            adapter: ModelAdapter<Table>,
            action: ChangeAction,
            authority: String,
        ) : this(
            dbRepresentable = adapter,
            action = action,
            authority = authority,
            changedFields = adapter.getPrimaryModelClause(
                model
            )
                // TODO: we should enforce this at operator group level.
                .filterIsInstance<BaseOperator.SingleValueOperator<Table>>()
        )

        override val uri: Uri by lazy {
            createUri {
                changedFields.forEach { operator ->
                    appendQueryParameter(
                        operator.nameAlias.query,
                        operator.value.toString(),
                    )
                }
            }
        }
    }
}

interface ContentNotificationDecoder {

    fun <Table : Any> decode(uri: Uri): ContentNotification<Table>?
}

internal fun contentDecoder(): ContentNotificationDecoder =
    DefaultContentNotificationDecoder

internal object DefaultContentNotificationDecoder : ContentNotificationDecoder {
    override fun <Table : Any> decode(uri: Uri): ContentNotification<Table>? {
        val tableName = uri.getQueryParameter(TABLE_QUERY_PARAM)

        if (tableName != null) {
            val fragment = uri.fragment
            val queryNames = uri.queryParameterNames
            val columns = queryNames.asSequence()
                .filter { it != TABLE_QUERY_PARAM }
                .map { key ->
                    val param = Uri.decode(uri.getQueryParameter(key))
                    val columnName = Uri.decode(key)
                    operator(
                        NameAlias.Builder(columnName).build(),
                        operation = Operation.Equals,
                        param
                    )
                }.toList()
            val action = fragment?.let { ChangeAction.valueOf(it) } ?: ChangeAction.NONE

            // model level change when we have column names in Uri
            return if (columns.isNotEmpty()) {
                ContentNotification.ModelChange(
                    dbRepresentable = FlowManager.getModelAdapterByTableName(tableName),
                    action = action,
                    authority = uri.authority ?: "",
                    changedFields = columns,
                )
            } else {
                ContentNotification.TableChange(
                    dbRepresentable = FlowManager.getModelAdapterByTableName(tableName),
                    action = action,
                    authority = uri.authority ?: "",
                )
            }
        } else {
            return null
        }
    }
}

fun interface ContentNotificationListener<Table : Any> {
    fun onChange(notification: ContentNotification<Table>)
}

@Suppress("UNCHECKED_CAST")
internal fun <Table : Any> ContentNotificationListener<*>.cast() =
    this as ContentNotificationListener<Table>
