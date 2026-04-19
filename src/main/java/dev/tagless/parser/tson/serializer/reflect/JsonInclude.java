package dev.tagless.parser.tson.serializer.reflect;

import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or a method value to be
 * serialized
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonInclude {

    /**
     * Get the field name in the JSON
     * document. If not provided, the field name
     * is used
     * @return the JSON name
     */
    @NotNull
    String value() default "";

    /**
     * Get the field adapters to use. This value
     * is a collection as a java type can have multiple
     * adapters depending on internal properties
     * @return the field JSON adapters
     */
    @NotNull
    Class<? extends JsonTypeAdapter>[] adapters() default {};
}
