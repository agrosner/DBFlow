package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

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
                column(AIContainerForeign_Table.id).is(autoIncrementContainer.id)).querySingle();
        assertNull(autoIncrementContainer.foreignModel);
        assertNull(autoIncrementContainer.container);

        AutoIncrementContainer foreignModel = new AutoIncrementContainer();
        foreignModel.a_id = 5;
        foreignModel.name = "foreign";
        foreignModel.save();
        assertTrue(foreignModel.exists());
        autoIncrementContainer.foreignModel = foreignModel;

        AutoIncrementContainer foreignKeyContainer = new AutoIncrementContainer();
        foreignKeyContainer.name = "container";
        foreignKeyContainer.a_id = 54;
        foreignKeyContainer.save();
        assertTrue(foreignKeyContainer.exists());
        autoIncrementContainer.setContainer(foreignKeyContainer);

        foreignKeyContainer.save();
        assertTrue(foreignKeyContainer.exists());
        assertNotNull(autoIncrementContainer.container);
        assertNotNull(autoIncrementContainer.foreignModel);

        Delete.table(AutoIncrementContainer.class);
    }
}
