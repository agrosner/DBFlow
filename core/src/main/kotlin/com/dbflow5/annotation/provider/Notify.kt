package com.dbflow5.annotation.provider

/**
 * Description: Annotates a method part of [com.dbflow5.annotation.provider.TableEndpoint]
 * that gets called back when changed. The method must return a Uri or an array of Uri[] to notify changed on
 * the content provider.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Notify(
        /**
         * @return The [com.dbflow5.annotation.provider.Notify.Method] notify
         */
        val notifyMethod: NotifyMethod,
        /**
         * @return Registers itself for the following paths. If a specific path is called for the specified
         * method, the method this annotation corresponds to will be called.
         */
        val paths: Array<String> = [])

enum class NotifyMethod {
    INSERT,
    UPDATE,
    DELETE
}