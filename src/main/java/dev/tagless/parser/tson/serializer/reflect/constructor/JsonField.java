package dev.tagless.parser.tson.serializer.reflect.constructor;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The field indicates a JSON entry which must
 * be passed to a constructor maked by {@link JsonConstructor}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface JsonField {

    /**
     * Get the JSON field value
     * @return the field in the JSON document
     */
    @NotNull
    String value();
}