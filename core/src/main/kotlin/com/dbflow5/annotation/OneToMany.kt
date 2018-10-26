package com.dbflow5.annotation

/**
 * Description: Describes a 1-many relationship. It applies to some method that returns a [List] of Model objects.
 * This annotation can handle loading, deleting, and saving when the current data changes. By default it will call the
 * associated method when the containing class operates.
 */
@Target(AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.SOURCE)
annotation class OneToMany(
        /**
         * @return The methods you wish to call it from. By default it's loaded out of the DB.
         */
        val oneToManyMethods: Array<OneToManyMethod> = [(OneToManyMethod.LOAD)],
        /**
         * @return The name of the list variable to use. If is left blank, we will remove the "get" and then decapitalize the remaining name.
         */
        val variableName: String = "",
        /**
         * @return If true, the code generated for this relationship done as efficiently as possible.
         * It will not work on nested relationships, caching, and other code that requires overriding of BaseModel or Model operations.
         */
        val efficientMethods: Boolean = true)

/**
 * The method to apply the OneToMany to.
 */
enum class OneToManyMethod {

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

