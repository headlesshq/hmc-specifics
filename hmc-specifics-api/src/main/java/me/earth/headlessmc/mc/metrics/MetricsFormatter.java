package me.earth.headlessmc.mc.metrics;

import me.earth.headlessmc.mc.util.ExtendedTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Formats the values collected by {@link MetricsService#collect(Collection)} as text or as JSON.
 */
public final class MetricsFormatter {
    private static final String NOT_AVAILABLE = "n/a";

    private MetricsFormatter() {
        throw new AssertionError();
    }

    /**
     * @param values metric names mapped to their values.
     * @return one line per metric, lists of maps are displayed as a table below the name of the metric.
     */
    public static String toText(Map<String, Object> values) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }

            builder.append(entry.getKey()).append(':');
            Object value = entry.getValue();
            String table = value instanceof List ? toTable((List<?>) value) : null;
            if (table != null) {
                for (String line : table.split("\n")) {
                    builder.append("\n  ").append(line);
                }
            } else {
                builder.append(' ').append(toText(value));
            }
        }

        return builder.toString();
    }

    public static String toText(Object value) {
        if (value == null) {
            return NOT_AVAILABLE;
        } else if (value instanceof Double || value instanceof Float) {
            return String.format(Locale.ROOT, "%.2f", ((Number) value).doubleValue());
        } else if (value instanceof Map) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }

                builder.append(entry.getKey()).append('=').append(toText(entry.getValue()));
            }

            return builder.toString();
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                return "none";
            }

            List<String> strings = new ArrayList<>(collection.size());
            for (Object element : collection) {
                strings.add(toText(element));
            }

            return "[" + String.join(", ", strings) + "]";
        }

        return value.toString();
    }

    /**
     * @return the list as a table, if it is a non-empty list of maps, otherwise {@code null}.
     */
    private static String toTable(List<?> list) {
        if (list.isEmpty()) {
            return null;
        }

        List<Map<?, ?>> rows = new ArrayList<>(list.size());
        for (Object element : list) {
            if (!(element instanceof Map)) {
                return null;
            }

            rows.add((Map<?, ?>) element);
        }

        ExtendedTable<Map<?, ?>> table = new ExtendedTable<>();
        for (Object key : rows.get(0).keySet()) {
            table.withColumn(String.valueOf(key), row -> toText(row.get(key)));
        }

        return table.addAll(rows).build();
    }

    /**
     * @param value a value consisting of Maps, Collections, Numbers, Booleans and Strings.
     * @return the value as JSON.
     */
    public static String toJson(Object value) {
        StringBuilder builder = new StringBuilder();
        appendJson(builder, value);
        return builder.toString();
    }

    private static void appendJson(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            builder.append(Double.isNaN(d) || Double.isInfinite(d) ? "null" : String.valueOf(d));
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else if (value instanceof Map) {
            builder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    builder.append(',');
                }

                first = false;
                appendString(builder, String.valueOf(entry.getKey()));
                builder.append(':');
                appendJson(builder, entry.getValue());
            }

            builder.append('}');
        } else if (value instanceof Collection) {
            builder.append('[');
            boolean first = true;
            for (Object element : (Collection<?>) value) {
                if (!first) {
                    builder.append(',');
                }

                first = false;
                appendJson(builder, element);
            }

            builder.append(']');
        } else {
            appendString(builder, value.toString());
        }
    }

    private static void appendString(StringBuilder builder, String string) {
        builder.append('"');
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
            }
        }

        builder.append('"');
    }

}
