package com.dbflow5.models

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ColumnMapReference
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.annotation.ForeignKeyReference
import com.dbflow5.annotation.NotNull
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.TypeConverter
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.LoadFromCursorListener

/**
 * Example of simple foreign key object with one foreign key object.
 */
@Table
class Blog(
    @PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
    @ForeignKey(saveForeignKeyModel = true) var author: Author? = null
)

/**
 * Parent used as foreign key reference.
 */
@Table
class Author(
    @PrimaryKey(autoincrement = true) var id: Int = 0,
    @Column(name = "first_name") var firstName: String = "",
    @Column(name = "last_name") var lastName: String = ""
)

/**
 * Example of simple foreign key object with its [ForeignKey] deferred.
 */
@Table
class BlogDeferred(
    @PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
    @ForeignKey(deferred = true) var author: Author? = null
)

/**
 * Class has example of single foreign key with [ForeignKeyReference] specified
 */
@Table
class BlogRef(
    @PrimaryKey var id: Int = 0, @PrimaryKey var name: String = "",
    @ForeignKey(
        references = arrayOf(
            ForeignKeyReference(
                columnName = "authorId", foreignKeyColumnName = "id",
                defaultValue = "not gonna work"
            )
        )
    )
    var author: Author? = null
)

/**
 * Class has example of single foreign key with [ForeignKeyReference] specified that is not the model object.
 */
@Table
class BlogRefNoModel(
    @PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
    @ForeignKey(
        references = arrayOf(
            ForeignKeyReference(
                columnName = "authorId",
                foreignKeyColumnName = "id",
                notNull = NotNull(onNullConflict = ConflictAction.FAIL)
            )
        ),
        tableClass = Author::class
    )
    var authorId: String? = null
)


/**
 * Class has example of single foreign key with [ForeignKeyReference] as [PrimaryKey]
 */
@Table
class BlogPrimary(@PrimaryKey @ForeignKey var author: Author? = null, @Column var id: Int = 0)

/**
 * Example of simple foreign key object with one foreign key object thats [ForeignKey.stubbedRelationship]
 *  and [ForeignKey.saveForeignKeyModel]
 */
@Table
class BlogStubbed(
    @PrimaryKey(autoincrement = true) var id: Int = 0, @Column var name: String = "",
    @ForeignKey(
        saveForeignKeyModel = true,
        onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.RESTRICT
    )
    var author: Author? = null,
    var setter: String = "",
    var getter: String = ""
) : LoadFromCursorListener {
    override fun onLoadFromCursor(cursor: FlowCursor) {

    }
}

class DoubleToDouble(val double: Double)

@TypeConverter
class DoubleConverter : com.dbflow5.converter.TypeConverter<Double, DoubleToDouble>() {
    override fun getDBValue(model: DoubleToDouble) = model.double

    override fun getModelValue(data: Double): DoubleToDouble = data.let { DoubleToDouble(data) }
}

class Location(
    var latitude: DoubleToDouble? = DoubleToDouble(0.0),
    var longitude: DoubleToDouble? = DoubleToDouble(0.0)
)

@Table
class Position(@PrimaryKey var id: Int = 0, @ColumnMap var location: Location? = null)

@Table
class Position2(
    @PrimaryKey var id: Int = 0,
    @ColumnMap(
        references = arrayOf(
            ColumnMapReference(
                columnName = "latitude", columnMapFieldName = "latitude",
                defaultValue = "40.6"
            ),
            ColumnMapReference(
                columnName = "longitude", columnMapFieldName = "longitude",
                defaultValue = "55.5"
            )
        )
    )
    var location: Location? = null
)

class Location2(
    var latitude: DoubleToDouble? = DoubleToDouble(0.0),
    var longitude: Double? = 0.0
)

@Table
class PositionWithTypeConverter(
    @PrimaryKey var id: Int = 0,
    @ColumnMap(
        references = [
            ColumnMapReference(
                columnName = "latitude",
                columnMapFieldName = "latitude", typeConverter = DoubleConverter::class
            ),
            ColumnMapReference(columnName = "longitude", columnMapFieldName = "longitude")]
    )
    var location: Location2? = null
)

@Table
class NotNullReferenceModel(
    @PrimaryKey var name: String = "",
    @NotNull @ForeignKey var model: SimpleModel? = null
)
