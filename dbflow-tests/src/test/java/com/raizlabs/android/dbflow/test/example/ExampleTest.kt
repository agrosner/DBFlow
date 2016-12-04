package com.raizlabs.android.dbflow.test.example

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * Description:
 */
class ExampleTest : FlowTestCase() {

    @Test
    fun testExample() {

        Delete.tables(Ant::class.java, Queen::class.java, Colony::class.java)

        val queen = Queen()
        queen.name = "Queenie"

        val colony = Colony()
        colony.name = "USOfAnts"

        // start a colony.
        colony.save()

        assertTrue(colony.exists())

        queen.colony = colony

        // associate queen with colony
        queen.save()
        assertTrue(queen.exists())

        val ant = Ant()
        ant.isMale = true
        ant.type = "Worker"
        ant.queen = queen
        ant.save()

        assertEquals(queen.myAnts.size.toLong(), 1)
        assertTrue(ant.exists())

        queen.delete()
        assertFalse(queen.exists())
        assertFalse(ant.exists())

        assertTrue(colony.exists())

        Delete.tables(Ant::class.java, Queen::class.java, Colony::class.java)
    }
}
