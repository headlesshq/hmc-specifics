package me.earth.headlessmc.mc;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ReflectionHelper {
    public static <T> Set<T> findAll(Object instance, Class<T> clazz) {
        Set<T> result = new LinkedHashSet<>();
        for (Class<?> c = instance.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (clazz.isAssignableFrom(field.getType())
                    // TODO: is this even needed?
                    || Arrays.stream(field.getType().getInterfaces())
                             .anyMatch(clazz::isAssignableFrom)) {
                    field.setAccessible(true);
                    try {
                        Object obj = field.get(instance);
                        if (clazz.isInstance(obj)) {
                            result.add(clazz.cast(obj));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
    }

}
