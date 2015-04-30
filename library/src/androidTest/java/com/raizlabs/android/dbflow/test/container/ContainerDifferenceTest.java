package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Description: Asserts values are handled same for container and adapter
 */
public class ContainerDifferenceTest extends FlowTestCase {

    public void testContainer() {
        Delete.table(AutoIncrementContainer.class);

        AIContainerForeign autoIncrementContainer = new AIContainerForeign();
        autoIncrementContainer.a_id = 5;
        autoIncrementContainer.name = "name";
        autoIncrementContainer.foreignModel = null;
        autoIncrementContainer.container = null;
        autoIncrementContainer.save();

        autoIncrementContainer = new Select().from(AIContainerForeign.class).where(
                Condition.column(AIContainerForeign$Table.ID).is(autoIncrementContainer.id)).querySingle();
        assertNull(autoIncrementContainer.foreignModel);
        assertNull(autoIncrementContainer.container);

        AutoIncrementContainer foreignModel = new AutoIncrementContainer();
        foreignModel.a_id = 5;
        foreignModel.name = "foreign";
        foreignModel.save();
        assertTrue(foreignModel.exists());
        autoIncrementContainer.foreignModel = foreignModel;

        ForeignKeyContainer<AutoIncrementContainer> foreignKeyContainer = new ForeignKeyContainer<>(AutoIncrementContainer.class);
        foreignKeyContainer.put(AutoIncrementContainer$Table.NAME, "container");
        foreignKeyContainer.put(AutoIncrementContainer$Table.A_ID, 54);
        foreignKeyContainer.save();
        assertTrue(foreignKeyContainer.exists());
        autoIncrementContainer.container = foreignKeyContainer;

        foreignKeyContainer.save();
        assertTrue(foreignKeyContainer.exists());
        assertNotNull(autoIncrementContainer.container);
        assertNotNull(autoIncrementContainer.foreignModel);

        Delete.table(AutoIncrementContainer.class);
    }
}
