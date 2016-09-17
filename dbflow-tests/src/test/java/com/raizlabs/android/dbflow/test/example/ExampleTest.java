package com.raizlabs.android.dbflow.test.example;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class ExampleTest extends FlowTestCase {

    @Test
    public void testExample() {

        Delete.tables(Ant.class, Queen.class, Colony.class);

        Queen queen = new Queen();
        queen.name = "Queenie";

        Colony colony = new Colony();
        colony.name = "USOfAnts";

        // start a colony.
        colony.save();

        assertTrue(colony.exists());

        queen.colony = colony;

        // associate queen with colony
        queen.save();
        assertTrue(queen.exists());

        Ant ant = new Ant();
        ant.isMale = true;
        ant.type = "Worker";
        ant.save();

        assertEquals(queen.getMyAnts().size(), 1);
        assertTrue(ant.exists());

        queen.delete();
        assertFalse(queen.exists());
        assertFalse(ant.exists());

        assertTrue(colony.exists());

        Delete.tables(Ant.class, Queen.class, Colony.class);
    }
}
