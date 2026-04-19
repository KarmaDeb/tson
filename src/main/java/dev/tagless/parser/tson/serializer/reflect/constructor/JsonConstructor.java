package dev.tagless.parser.tson.serializer.reflect.constructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The constructor marks a class constructor
 * method as the one to be used when de-serializing
 * and loading a serialized JSON object. If no constructor
 * is provided or not available for specific version,
 * reflection is used instead (this also applies for fields
 * not specified in the constructor as {@link JsonField}).
 * A static method can be also be marked as constructor, or
 * an instance method (which requires a no-args constructor)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface JsonConstructor {

    /**
     * Get the serializer version of this
     * constructor
     * @return the serializer version
     */
    long value();
}
