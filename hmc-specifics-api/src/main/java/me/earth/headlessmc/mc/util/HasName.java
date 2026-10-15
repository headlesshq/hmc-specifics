package me.earth.headlessmc.mc.util;

public interface HasName {
    static <T extends HasName> T getByName(String name, Iterable<T> nameables) {
        for (T t : nameables) {
            if (name.equals(t.getName())) {
                return t;
            }
        }

        return null;
    }

    String getName();

}
