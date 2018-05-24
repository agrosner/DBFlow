package com.dbflow5.contentprovider.annotation

/**
 * Description: Defines the URI for a content provider.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.SOURCE)
annotation class ContentUri(
        /**
         * @return the path of this ContentUri. ex: notes/#, notes/1, etc. Must be unique within a [TableEndpoint]
         */
        val path: String,
        /**
         * @return The type of content that this uri is associated with. Ex: [ContentType.VND_SINGLE]
         */
        val type: String,
        /**
         * @return If the path defines "#", then we use these numbers to find them in the same order as
         * where column.
         */
        val segments: Array<PathSegment> = [],
        /**
         * @return false if you wish to not allow queries from the specified URI.
         */
        val queryEnabled: Boolean = true,
        /**
         * @return false if you wish to prevent inserts.
         */
        val insertEnabled: Boolean = true,
        /**
         * @return false if you wish to prevent deletion.
         */
        val deleteEnabled: Boolean = true,
        /**
         * @return false if you wish to prevent updates.
         */
        val updateEnabled: Boolean = true)

/**
 * Provides some handy constants for defining a [.type]
 */
object ContentType {

    const val VND_MULTIPLE = "vnd.android.cursor.dir/"

    const val VND_SINGLE = "vnd.android.cursor.item/"
}

/**
 * Defines the path segment that we use when we specify "#" in the path.
 */
annotation class PathSegment(
        /**
         * @return The number segment this corresponds to.
         */
        val segment: Int,
        /**
         * @return The column name that this segment will use.
         */
        val column: String)