package dev.tagless.parser.tson.serializer.reflect.adatper.impl.primitive;

import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * charseq element adapter for JSON
 */
public final class JsonCharSeqAdapter implements JsonTypeAdapter<CharSequence, JsonPrimitive> {

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

        return CharSequence.class.isAssignableFrom(type.getClass());
    }

    /**
     * Adapt the java type
     *
     * @param type the type
     * @return the adapted java type
     */
    @Override
    public @NotNull Optional<JsonPrimitive> adapt(final Object type) {
        if (!(type instanceof CharSequence s)) {
            return Optional.empty();
        }

        return Optional.of(new JsonPrimitive(s.toString()));
    }

    /**
     * Resolve the JSON value
     * into the java type
     *
     * @param type the JSON type
     * @return the java type
     */
    @Override
    public @NotNull Optional<CharSequence> resolve(final @NotNull JsonElement type) {
        if (!type.isString()) {
            return Optional.empty();
        }

        return Optional.of(type.asString());
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
        return jsonElement.isString();
    }
}
