package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void testEnumQuery_basicWhereEqual() throws Exception {
        Delete.table(EnumModel.class);

        EnumModel easy = new EnumModel();
        easy.setDifficulty(EnumModel.Difficulty.EASY);
        easy.save();

        EnumModel medium = new EnumModel();
        medium.setDifficulty(EnumModel.Difficulty.MEDIUM);
        medium.save();

        EnumModel hard = new EnumModel();
        hard.setDifficulty(EnumModel.Difficulty.HARD);
        hard.save();

        EnumModel result = SQLite.select()
            .from(EnumModel.class)
            .where(EnumModel_Table.difficulty.eq(EnumModel.Difficulty.MEDIUM))
            .querySingle();

        assertNotNull(result);
        assertEquals(result.id, medium.id);

        Delete.table(EnumModel.class);
    }

    @Test
    public void testEnumQuery_basicWhereIn() throws Exception {
        Delete.table(EnumModel.class);

        EnumModel easy = new EnumModel();
        easy.setDifficulty(EnumModel.Difficulty.EASY);
        easy.save();

        EnumModel medium = new EnumModel();
        medium.setDifficulty(EnumModel.Difficulty.MEDIUM);
        medium.save();

        EnumModel hard = new EnumModel();
        hard.setDifficulty(EnumModel.Difficulty.HARD);
        hard.save();

        List<EnumModel> result = SQLite.select()
            .from(EnumModel.class)
            .where(EnumModel_Table.difficulty.in(EnumModel.Difficulty.MEDIUM, EnumModel.Difficulty.HARD))
            .orderBy(OrderBy.fromProperty(EnumModel_Table.difficulty).ascending())
            .queryList();

        assertNotNull(result);
        assertEquals(result.get(0).id, medium.id);
        assertEquals(result.get(1).id, hard.id);

        Delete.table(EnumModel.class);
    }
}
