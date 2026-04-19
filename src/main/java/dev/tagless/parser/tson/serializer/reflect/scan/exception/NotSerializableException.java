package dev.tagless.parser.tson.serializer.reflect.scan.exception;

import dev.tagless.parser.tson.serializer.reflect.JsonSerializable;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * The exception is thrown when the object scan tries to process
 * an object which does not know how to scan. There are two ways of
 * making an object serializable, the first and recommended is by
 * annotating the class with {@link JsonSerializable}, the second is by
 * registered a specialized {@link JsonTypeAdapter} which can read
 * the object manually
 */
@SuppressWarnings("unused")
public class NotSerializableException extends RuntimeException {

    /**
     * Initialize the exception
     * @param object the object which is trying
     *               to be serialized
     */
    public NotSerializableException(final @NotNull Object object) {
        super(String.format("Cannot serialize using json object of type: %s", object.getClass().getCanonicalName()));
    }

    /**
     * Initialize the exception
     * @param anonClass the anonymous class which
     *                  is trying to be serialized
     */
    public NotSerializableException(final @NotNull Class<?> anonClass) {
        super(String.format("Cannot serialize anonymous/abstract class: %s", anonClass.getCanonicalName()));
    }

    /**
     * Initialize the exception
     * @param cause the exception cause
     */
    public NotSerializableException(final @NotNull Throwable cause) {
        super(cause);
    }
}
