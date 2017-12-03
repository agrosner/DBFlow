package com.raizlabs.dbflow5.dbflow.models

import android.database.Cursor
import com.raizlabs.dbflow5.dbflow.TestDatabase
import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.ColumnMap
import com.raizlabs.dbflow5.annotation.ColumnMapReference
import com.raizlabs.dbflow5.annotation.ConflictAction
import com.raizlabs.dbflow5.annotation.ForeignKey
import com.raizlabs.dbflow5.annotation.ForeignKeyAction
import com.raizlabs.dbflow5.annotation.ForeignKeyReference
import com.raizlabs.dbflow5.annotation.NotNull
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.query.LoadFromCursorListener

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
