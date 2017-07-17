package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Description: Describes a 1-many relationship. It applies to some method that returns a {@link List} of Model objects.
 * This annotation can handle loading, deleting, and saving when the current data changes. By default it will call the
 * associated method when the containing class operates.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface OneToMany {

    /**
     * The method to apply the OneToMany to.
     */
    enum Method {

        /**
         * Load this relationship when the parent model loads from the database. This is called before the OnLoadFromCursor
         * method, but after other columns load.
         */
        LOAD,

        /**
         * Inserts code to delete the results returned from the List relationship when the parent model is deleted.
         */
        DELETE,

        /**
         * Inserts code to save the list of models when the parent model is saved.
         */
        SAVE,

        /**
         * Shorthand to support all options.
         */
        ALL
    }

    /**
     * @return The methods you wish to call it from. By default it's loaded out of the DB.
     */
    Method[] methods() default Method.LOAD;

    /**
     * @return The name of the list variable to use. If is left blank, we will remove the "get" and then decapitalize the remaining name.
     */
    String variableName() default "";

    /**
     * @return If true, the underlying variable that we use is private, requiring us to provide
     * a setter for it.
     */
    boolean isVariablePrivate() default false;

    /**
     * @return If true, the code generated for this relationship done as efficiently as possible.
     * It will not work on nested relationships, caching, and other code that requires overriding of BaseModel or Model operations.
     */
    boolean efficientMethods() default true;
}
