package com.raizlabs.android.dbflow.models

import android.database.Cursor
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ColumnMap
import com.raizlabs.android.dbflow.annotation.ColumnMapReference
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.listener.LoadFromCursorListener

/**
 * Example of simple foreign key object with one foreign key object.
 */
@Table(database = TestDatabase::class)
class Blog(@PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
           @ForeignKey var author: Author? = null)

/**
 * Parent used as foreign key reference.
 */
@Table(database = TestDatabase::class)
class Author(@PrimaryKey(autoincrement = true) var id: Int = 0,
             @Column(name = "first_name") var firstName: String = "",
             @Column(name = "last_name") var lastName: String = "")

/**
 * Example of simple foreign key object with its [ForeignKey] deferred.
 */
@Table(database = TestDatabase::class)
class BlogDeferred(@PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
                   @ForeignKey(deferred = true) var author: Author? = null)

/**
 * Class has example of single foreign key with [ForeignKeyReference] specified
 */
@Table(database = TestDatabase::class)
class BlogRef(@PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
              @ForeignKey(references = arrayOf(ForeignKeyReference(columnName = "authorId", foreignKeyColumnName = "id")))
              var author: Author? = null)

/**
 * Class has example of single foreign key with [ForeignKeyReference] specified that is not the model object.
 */
@Table(database = TestDatabase::class)
class BlogRefNoModel(@PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
                     @ForeignKey(references = arrayOf(ForeignKeyReference(columnName = "authorId", foreignKeyColumnName = "id", notNull = NotNull(onNullConflict = ConflictAction.FAIL))),
                         tableClass = Author::class)
                     var authorId: String? = null)


/**
 * Class has example of single foreign key with [ForeignKeyReference] as [PrimaryKey]
 */
@Table(database = TestDatabase::class)
class BlogPrimary(@PrimaryKey @ForeignKey var author: Author? = null, @Column var id: Int = 0)

/**
 * Example of simple foreign key object with one foreign key object thats [ForeignKey.stubbedRelationship]
 *  and [ForeignKey.deleteForeignKeyModel] and [ForeignKey.saveForeignKeyModel]
 */
@Table(database = TestDatabase::class)
class BlogStubbed(@PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
                  @ForeignKey(stubbedRelationship = true, deleteForeignKeyModel = true, saveForeignKeyModel = true,
                      onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.RESTRICT)
                  var author: Author? = null) : LoadFromCursorListener {
    override fun onLoadFromCursor(cursor: Cursor) {

    }
}

class Location(var latitude: Double = 0.0, var longitude: Double = 0.0)

@Table(database = TestDatabase::class)
class Position(@PrimaryKey var id: Int = 0, @ColumnMap var location: Location? = null)

@Table(database = TestDatabase::class)
class Position2(@PrimaryKey var id: Int = 0,
                @ColumnMap(references = arrayOf(
                    ColumnMapReference(columnName = "latitude", columnMapFieldName = "latitude"),
                    ColumnMapReference(columnName = "longitude", columnMapFieldName = "longitude")))
                var location: Location? = null)
