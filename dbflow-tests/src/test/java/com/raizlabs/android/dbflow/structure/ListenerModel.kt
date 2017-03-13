package com.raizlabs.android.dbflow.structure

import android.content.ContentValues
import android.database.Cursor

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.listener.ContentValuesListener
import com.raizlabs.android.dbflow.structure.listener.LoadFromCursorListener
import com.raizlabs.android.dbflow.structure.listener.SQLiteStatementListener
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ListenerModel : TestModel1(), LoadFromCursorListener, SQLiteStatementListener, ContentValuesListener {

    private var loadFromCursorListener: LoadFromCursorListener? = null
    private var sqLiteStatementListener: SQLiteStatementListener? = null
    private var contentValuesListener: ContentValuesListener? = null

    fun registerListeners(sqLiteStatementListener: SQLiteStatementListener,
                          contentValuesListener: ContentValuesListener) {

        this.sqLiteStatementListener = sqLiteStatementListener
        this.contentValuesListener = contentValuesListener
    }

    fun registerLoadFromCursorListener(loadFromCursorListener: LoadFromCursorListener) {
        this.loadFromCursorListener = loadFromCursorListener
    }

    override fun onBindToContentValues(contentValues: ContentValues) {
        contentValuesListener!!.onBindToContentValues(contentValues)
    }

    override fun onBindToInsertValues(contentValues: ContentValues) {
        contentValuesListener!!.onBindToInsertValues(contentValues)
    }

    override fun onLoadFromCursor(cursor: Cursor) {
        loadFromCursorListener!!.onLoadFromCursor(cursor)
    }

    override fun onBindToStatement(databaseStatement: DatabaseStatement) {
        sqLiteStatementListener!!.onBindToStatement(databaseStatement)
    }

    override fun onBindToInsertStatement(databaseStatement: DatabaseStatement) {
        sqLiteStatementListener!!.onBindToInsertStatement(databaseStatement)
    }
}
