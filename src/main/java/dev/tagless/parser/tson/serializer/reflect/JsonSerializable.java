package dev.tagless.parser.tson.serializer.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as serializable by the
 * TSON JSON serializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("unused")
public @interface JsonSerializable {

    /**
     * Get the current version of the
     * serializer. This is used later for
     * object re-construction
     * @return the serializer version
     */
    long value();

    /**
     * Get the name of the object
     * @return the name
     */
    @NotNull
    String name() default "";

    /**
     * Perform a deep field scan. This is useful
     * for constructor-less classes with inherited
     * fields which require their values to be persisted
     * @return if the serializable object scan fields deeply
     */
    boolean deepScan() default false;

    /**
     * Set the serializer to include all fields
     * by default.
     * @return if all fields should be included by default
     */
    boolean includeByDefault() default false;

    /**
     * Set the serializer to exclude all fields
     * by default, unless specified. This settings
     * invalidates {@link #includeByDefault()}
     * @return if all fields should be excluded unless
     * specified
     */
    boolean excludeByDefault() default false;
}
