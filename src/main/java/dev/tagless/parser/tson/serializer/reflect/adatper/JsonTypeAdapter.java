package dev.tagless.parser.tson.serializer.reflect.adatper;

import dev.tagless.parser.tson.element.JsonElement;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * JSON type adapters helps to adapt a java type
 * to a JSON serializable type and vice versa. For instance
 * UUID -> JSON string | JSON string -> UUID. Every type
 * adapter is also a predicate of {@link JsonElement} and
 * sub-predicate of JavaType
 * @param <JavaType> the java type
 * @param <JsonType> the JSON type
 */
public interface JsonTypeAdapter<JavaType, JsonType extends JsonElement> extends Predicate<JsonElement> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param type the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    boolean subTest(final Object type);

    /**
     * Adapt the java type
     * @param type the type
     * @return the adapted java type
     */
    @NotNull
    Optional<JsonType> adapt(final Object type);

    /**
     * Represent the java type
     * @param type the type
     * @return the represented java type
     */
    @NotNull
    default Optional<? extends JsonElement> represent(final Object type) {
        return this.adapt(type);
    }

    /**
     * Resolve the JSON value
     * into the java type
     * @param type the JSON type
     * @return the java type
     */
    @NotNull
    Optional<JavaType> resolve(final @NotNull JsonElement type);
}
