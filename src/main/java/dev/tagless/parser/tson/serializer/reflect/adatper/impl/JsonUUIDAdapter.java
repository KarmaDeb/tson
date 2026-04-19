package dev.tagless.parser.tson.serializer.reflect.adatper.impl;

import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * uuid element adapter for JSON
 */
public final class JsonUUIDAdapter implements JsonTypeAdapter<UUID, JsonObject> {


    /**
     * Evaluates this predicate on the given argument.
     *
     * @param type the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean subTest(Object type) {
        return type instanceof UUID;
    }

    /**
     * Adapt the java type
     *
     * @param type the type
     * @return the adapted java type
     */
    @Override
    public @NotNull Optional<JsonObject> adapt(final Object type) {
        if (!(type instanceof UUID u)) {
            return Optional.empty();
        }

        JsonObject object = new JsonObject();
        object.set("meta.adapter", this.getClass().getCanonicalName());
        object.set("most", u.getMostSignificantBits());
        object.set("least", u.getLeastSignificantBits());

        return Optional.of(object);
    }

    /**
     * Represent the java type
     *
     * @param type the type
     * @return the represented java type
     */
    @Override
    public @NotNull Optional<JsonElement> represent(final Object type) {
        if (!(type instanceof UUID u)) {
            return Optional.empty();
        }

        return Optional.of(
                new JsonPrimitive(u.toString())
        );
    }

    /**
     * Resolve the JSON value
     * into the java type
     *
     * @param type the JSON type
     * @return the java type
     */
    @Override
    public @NotNull Optional<UUID> resolve(@NotNull JsonElement type) {
        JsonObject object = type.object();

        long most = object.get("most").asNumber().longValue();
        long least = object.get("least").asNumber().longValue();

        return Optional.of(new UUID(most, least));
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param element the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(final JsonElement element) {
        if (!element.isObject()) {
            return false;
        }

        JsonObject object = element.object();
        return this.getClass().getCanonicalName()
                .equals(object.get("meta.adapter").asString());
    }
}
