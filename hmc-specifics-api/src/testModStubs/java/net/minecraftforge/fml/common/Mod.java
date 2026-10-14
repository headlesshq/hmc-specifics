package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stub, provided by Forge at runtime.
 * Forge 1.13+ reads {@code value}, Forge 1.8.9 - 1.12.2 reads {@code modid}, {@code name} and {@code version}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value() default "";

    String modid() default "";

    String name() default "";

    String version() default "";

}
