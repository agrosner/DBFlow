package com.grosner.dbflow.test.utils;

import junit.framework.Assert;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class AssertUtils {

    public static void assertThrowsException(Class<? extends Exception> exceptionClass, Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            Assert.assertEquals(exceptionClass, e.getClass());
        }
    }
}
