package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.*

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
                     @ForeignKey(references = arrayOf(ForeignKeyReference(columnName = "authorId", foreignKeyColumnName = "id")),
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
                  var author: Author? = null)