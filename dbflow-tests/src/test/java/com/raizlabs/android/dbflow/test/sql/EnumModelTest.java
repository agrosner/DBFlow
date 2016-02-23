package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Description: Tests enums
 */
public class EnumModelTest extends FlowTestCase {

    @Test
    public void testEnumModel() {
        Delete.table(EnumModel.class);

        EnumModel enumModel = new EnumModel();
        enumModel.setDifficulty(EnumModel.Difficulty.EASY);
        enumModel.save();

        enumModel = new Select().from(EnumModel.class).querySingle();
        assertEquals(EnumModel.Difficulty.EASY, enumModel.getDifficulty());

        Delete.table(EnumModel.class);
    }
}
