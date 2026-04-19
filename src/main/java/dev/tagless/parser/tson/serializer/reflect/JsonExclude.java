package dev.tagless.parser.tson.serializer.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or a method value to be
 * serialized. The annotation can also be applied
 * to object types which should never be included
 * into the serialization process
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface JsonExclude {
}
