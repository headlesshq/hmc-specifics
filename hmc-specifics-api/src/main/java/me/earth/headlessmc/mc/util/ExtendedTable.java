package me.earth.headlessmc.mc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Assists with building aligned tables.
 *
 * @param <T> the type of the objects used to create the table.
 */
public class ExtendedTable<T> {
    protected final List<Column<T>> columns = new ArrayList<>();
    protected final List<T> elements = new ArrayList<>();

    public ExtendedTable<T> withColumn(String name, Function<T, String> column) {
        columns.add(new Column<>(name, column));
        return this;
    }

    @SafeVarargs
    public final ExtendedTable<T> add(T... elements) {
        this.elements.addAll(Arrays.asList(elements));
        return this;
    }

    public ExtendedTable<T> addAll(Iterable<T> elements) {
        elements.forEach(this.elements::add);
        return this;
    }

    public ExtendedTable<T> withInt(String name, Function<T, Integer> column) {
        columns.add(new Column<>(name, t -> column.apply(t).toString()));
        return this;
    }

    public ExtendedTable<T> withBool(String name, Function<T, Boolean> column) {
        columns.add(new Column<>(name, t -> column.apply(t).toString()));
        return this;
    }

    public ExtendedTable<T> insert(String name,
                                   String at,
                                   Function<T, String> column) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).name.equalsIgnoreCase(at)) {
                columns.add(i, new Column<>(name, column));
                return this;
            }
        }

        return this;
    }

    public String build() {
        return build(String::length);
    }

    public String buildCalculatingAnsiWidth() {
        return build(ExtendedTable::getLengthWithoutAnsi);
    }

    private String build(Function<String, Integer> length) {
        List<Integer> columnWidths = new ArrayList<>(this.columns.size());
        List<List<String>> columns = this.columns.stream().map(e -> {
            List<String> entries = this.elements.isEmpty()
                    ? new ArrayList<>(Collections.singletonList("-"))
                    : this.elements.stream()
                        .map(e.function)
                        .map(str -> str == null ? "null" : str)
                        .collect(Collectors.toList());
            entries.add(0, String.valueOf(e.name));
            // let's hope the Terminal uses a fixed-width font
            columnWidths.add(entries.stream()
                    .map(length)
                    .max(Integer::compareTo)
                    .get());

            return entries;
        }).collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();
        for (int i = 0; !columns.isEmpty() && i < columns.get(0).size(); i++) {
            for (int j = 0; j < columns.size(); j++) {
                String entry = columns.get(j).get(i);
                int width = columnWidths.get(j);
                builder.append(entry);
                // last column doesn't need to be filled up
                if (j == columns.size() - 1) {
                    continue;
                }

                for (int k = 0; k < width - length.apply(entry) + 3; k++) {
                    builder.append(' ');
                }
            }

            // no need to append a linebreak on the last row
            if (i < columns.get(0).size() - 1) {
                builder.append('\n');
            }
        }

        return builder.toString();
    }

    public static int getLengthWithoutAnsi(String string) {
        return stripAnsi(string).length();
    }

    public static String stripAnsi(String string) {
        return string.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    protected static class Column<T> {
        public final Function<T, String> function;
        public final String name;

        public Column(String name, Function<T, String> function) {
            this.function = function;
            this.name = name;
        }
    }

}
