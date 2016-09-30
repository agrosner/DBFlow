package com.raizlabs.android.dbflow.converter;

import com.raizlabs.android.dbflow.sql.SQLiteType;

import java.util.List;

/**
 * Description: Responsible for converting a {@link SQLiteType#TEXT} into a {@link List}
 *
 * @author Andrew Grosner (fuzz)
 */

public interface ListConverter<TItem> {

    List<TItem> toList(String input);

    String fromList(List<TItem> list);
}
