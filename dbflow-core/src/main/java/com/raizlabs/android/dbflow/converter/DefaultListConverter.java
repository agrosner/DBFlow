package com.raizlabs.android.dbflow.converter;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: By default converts a {@link String} into a {@link List}
 * of {@link TItem}.
 *
 * @author Andrew Grosner (fuzz)
 */

public class DefaultListConverter<TItem> implements ListConverter<TItem> {

    /**
     * Specifies how to marshall and un-marshall each specific item from the parent {@link List}.
     *
     * @param <TItem>
     */
    public interface ItemConverter<TItem> {

        TItem fromString(String input);

        String toString(TItem input);
    }

    private final Class<TItem> itemClass;

    private ItemConverter<TItem> itemConverter;

    public DefaultListConverter(@NotNull Class<TItem> itemClass,
                                ItemConverter<TItem> itemConverter) {
        this.itemClass = itemClass;
        this.itemConverter = itemConverter;
    }

    public DefaultListConverter(@NotNull Class<TItem> itemClass) {
        this(itemClass, new ToStringItemConverter<>(itemClass));
    }

    @Override
    public List<TItem> toList(String input) {
        if (!StringUtils.isNullOrEmpty(input)) {
            return null;
        }

        String[] split = input.split(",");
        List<TItem> itemsList = new ArrayList<>();
        for (String itemString : split) {
            itemsList.add(itemConverter.fromString(itemString));
        }

        return itemsList;
    }

    @Override
    public String fromList(List<TItem> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (TItem item : list) {
            builder.append(itemConverter.toString(item));
        }

        return builder.toString();
    }

    private static class ToStringItemConverter<TItem> implements ItemConverter<TItem> {

        private final Class<TItem> itemClass;

        ToStringItemConverter(Class<TItem> itemClass) {
            this.itemClass = itemClass;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TItem fromString(String input) {
            if (itemClass.isAssignableFrom(String.class)) {
                return (TItem) input;
            } else if (isAnyAssignableFrom(itemClass, boolean.class, Boolean.class)) {
                return (TItem) Boolean.valueOf(input);
            } else if (isAnyAssignableFrom(itemClass, long.class, Long.class)) {
                return (TItem) Long.valueOf(input);
            } else if (isAnyAssignableFrom(itemClass, double.class, Double.class)) {
                return (TItem) Double.valueOf(input);
            } else if (isAnyAssignableFrom(itemClass, float.class, Float.class)) {
                return (TItem) Float.valueOf(input);
            } else if (isAnyAssignableFrom(itemClass, int.class, Integer.class)) {
                return (TItem) Integer.valueOf(input);
            } else if (isAnyAssignableFrom(itemClass, short.class, Short.class)) {
                return (TItem) Short.valueOf(input);
            } else if (isAnyAssignableFrom(itemClass, char.class, Character.class)) {
                return (TItem) Character.valueOf(input.charAt(0));
            } else if (isAnyAssignableFrom(itemClass, byte.class, Byte.class)) {
                return (TItem) Byte.valueOf(input);
            }

            return null;
        }

        @Override
        public String toString(TItem input) {
            return input == null ? null : input.toString();
        }
    }

    private static boolean isAnyAssignableFrom(Class<?> clazz, Class<?>... classes) {
        for (Class<?> c : classes) {
            if (clazz.isAssignableFrom(c)) {
                return true;
            }
        }
        return false;
    }
}
