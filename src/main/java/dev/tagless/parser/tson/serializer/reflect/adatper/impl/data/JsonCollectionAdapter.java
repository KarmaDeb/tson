package dev.tagless.parser.tson.serializer.reflect.adatper.impl.data;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import dev.tagless.parser.tson.serializer.reflect.scan.ObjectScan;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * collection element adapter for JSON
 */
public final class JsonCollectionAdapter<T> implements JsonTypeAdapter<Collection<T>, JsonObject> {

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

        return type instanceof Collection<?>;
    }

    /**
     * Adapt the java type
     *
     * @param object the type
     * @return the adapted java type
     */
    @Override
    public @NotNull Optional<JsonObject> adapt(final Object object) {
        if (!(object instanceof Collection<?> c)) {
            return Optional.empty();
        }

        Class<?> type = c.getClass();

        JsonObject adapted = new JsonObject();
        adapted.set("meta.adapter", this.getClass().getCanonicalName());
        adapted.set("meta.type", type.getCanonicalName());

        JsonArray values = new JsonArray();
        for (Object element : c) {
            if (element == null) {
                values.addNull();
                continue;
            }

            JsonElement serialized = ObjectScan.serialize(element);
            values.add(serialized);
        }

        adapted.set("values", values);

        return Optional.of(adapted);
    }

    /**
     * Represent the java type
     *
     * @param object the type
     * @return the represented java type
     */
    @Override
    public @NotNull Optional<JsonElement> represent(final Object object) {
        if (!(object instanceof Collection<?> c)) {
            return Optional.empty();
        }

        JsonArray array = new JsonArray();
        for (Object element : c) {
            array.add(ObjectScan.represent(element));
        }

        return Optional.of(array);
    }

    /**
     * Resolve the JSON value
     * into the java type
     *
     * @param jsonElement the JSON type
     * @return the java type
     */
    @Override @SuppressWarnings("unchecked")
    public @NotNull Optional<Collection<T>> resolve(final @NotNull JsonElement jsonElement) {
        JsonObject object = jsonElement.object();

        JsonArray values = object.get("values").array();
        try {
            // noinspection rawtypes
            Class<? extends Collection> componentType = Class.forName(object.get("meta.type").asString())
                    .asSubclass(Collection.class);
            Collection<Object> collection = componentType.getConstructor().newInstance();

            for (JsonElement element : values) {
                if (element.isNull()) {
                    collection.add(null);
                    continue;
                }

                Object loaded = ObjectScan.deserialize(element);
                collection.add(loaded);
            }

            return Optional.of((Collection<T>) collection);
        } catch (ReflectiveOperationException | NoClassDefFoundError ex) {
            return Optional.empty();
        }
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

        JsonObject object = (JsonObject) element;
        String adapter = object.get("meta.adapter").asString();
        return this.getClass().getCanonicalName().equals(adapter);
    }
}
