package dev.tagless.parser.tson.serializer.reflect.constructor;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The setter indicates a setter method which
 * MUST be used for a field to be set onto an
 * instance
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JsonSetter {

    /**
     * Get the JSON field value
     * @return the field in the JSON document
     */
    @NotNull
    String value();

    /**
     * Apply the logic only if the field
     * value is not null
     * @return if the logic applies when
     * the field is not null
     */
    boolean ifNotNull() default false;
}