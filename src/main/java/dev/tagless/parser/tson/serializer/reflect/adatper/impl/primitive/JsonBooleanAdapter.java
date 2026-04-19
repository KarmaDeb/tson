package dev.tagless.parser.tson.serializer.reflect.adatper.impl.primitive;

import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * bool element adapter for JSON
 */
public final class JsonBooleanAdapter implements JsonTypeAdapter<Boolean, JsonPrimitive> {

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

        Class<?> clazz = type.getClass();
        return clazz.equals(Boolean.class);
    }

    /**
     * Adapt the java type
     *
     * @param type the type
     * @return the adapted java type
     */
    @Override
    public @NotNull Optional<JsonPrimitive> adapt(final Object type) {
        if (type == null || !type.getClass().equals(Boolean.class)) {
            return Optional.empty();
        }

        return Optional.of(new JsonPrimitive((boolean) type));
    }

    /**
     * Resolve the JSON value
     * into the java type
     *
     * @param type the JSON type
     * @return the java type
     */
    @Override
    public @NotNull Optional<Boolean> resolve(final @NotNull JsonElement type) {
        if (!type.isBoolean()) {
            return Optional.empty();
        }

        return Optional.of(type.asBoolean());
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param jsonElement the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(final JsonElement jsonElement) {
        return jsonElement.isBoolean();
    }
}
