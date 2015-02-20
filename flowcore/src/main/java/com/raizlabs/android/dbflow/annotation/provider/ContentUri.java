package com.raizlabs.android.dbflow.annotation.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Defines the URI for a content provider.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface ContentUri {

    /**
     * Provides some handy constants for defining a {@link #type()}
     */
    public static class ContentType {

        public static final String VND_MULTIPLE = "vnd.android.cursor.dir/";

        public static final String VND_SINGLE = "vnd.android.cursor.item/";
    }

    /**
     * Defines the path segment that we use when we specify "#" in the path.
     */
    public @interface PathSegment {

        /**
         * @return The number segment this corresponds to.
         */
        int segment();

        /**
         * @return The column name that this segment will use.
         */
        String column();
    }

    /**
     * @return the path of this ContentUri. ex: notes/#, notes/1, etc. Must be unique within a {@link com.raizlabs.android.dbflow.annotation.provider.TableEndpoint}
     */
    String path();

    /**
     * @return The type of content that this uri is associated with. Ex: {@link com.raizlabs.android.dbflow.annotation.provider.ContentUri.ContentType#VND_SINGLE}
     */
    String type();

    /**
     * @return If the path defines "#", then we use these numbers to find them in the same order as
     * where column.
     */
    PathSegment[] segments() default {};

    /**
     * @return false if you wish to not allow queries from the specified URI.
     */
    boolean queryEnabled() default true;

    /**
     * @return false if you wish to prevent inserts.
     */
    boolean insertEnabled() default true;

    /**
     * @return false if you wish to prevent deletion.
     */
    boolean deleteEnabled() default true;

    /**
     * @return false if you wish to prevent updates.
     */
    boolean updateEnabled() default true;

}
