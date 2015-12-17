package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Description: Tests enums
 */
public class EnumModelTest extends FlowTestCase {

    public void testEnumModel() {
        Delete.table(EnumModel.class);

        EnumModel enumModel = new EnumModel();
        enumModel.difficulty = EnumModel.Difficulty.EASY;
        enumModel.save();

        enumModel = new Select().from(EnumModel.class).querySingle();
        assertEquals(EnumModel.Difficulty.EASY, enumModel.difficulty);

        Delete.table(EnumModel.class);
    }
}
