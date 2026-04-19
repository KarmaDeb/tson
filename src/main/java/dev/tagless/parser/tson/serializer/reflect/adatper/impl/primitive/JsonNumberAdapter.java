package dev.tagless.parser.tson.serializer.reflect.adatper.impl.primitive;

import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * number element adapter for JSON
 */
public final class JsonNumberAdapter implements JsonTypeAdapter<Number, JsonPrimitive> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param type the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean subTest(final Object type) {
        if (type == null) {
            return false;
        }

        return Number.class.isAssignableFrom(type.getClass()) ||
                (type.getClass().isPrimitive() &&
                        !type.getClass().equals(Boolean.class) &&
                        !type.getClass().equals(Character.class)
                );
    }

    /**
     * Adapt the java type
     *
     * @param type the type
     * @return the adapted java type
     */
    @Override
    public @NotNull Optional<JsonPrimitive> adapt(Object type) {
        if (!this.subTest(type)) {
            return Optional.empty();
        }

        return Optional.of(new JsonPrimitive((Number) type));
    }

    /**
     * Resolve the JSON value
     * into the java type
     *
     * @param type the JSON type
     * @return the java type
     */
    @Override
    public @NotNull Optional<Number> resolve(final @NotNull JsonElement type) {
        if (!type.isNumber()) {
            return Optional.empty();
        }

        return Optional.of(type.asNumber());
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param jsonElement the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(JsonElement jsonElement) {
        return jsonElement.isNumber();
    }
}
