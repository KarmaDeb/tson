package dev.tagless.parser.tson.serializer.reflect.adatper.impl.data;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import dev.tagless.parser.tson.serializer.reflect.scan.ObjectScan;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Optional;

/**
 * array[] element adapter for JSON
 */
public final class JsonArrayAdapter<T> implements JsonTypeAdapter<T, JsonObject> {

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
        return clazz.isArray();
    }

    /**
     * Adapt the java type
     *
     * @param object the type
     * @return the adapted java type
     */
    @Override
    public @NotNull Optional<JsonObject> adapt(final Object object) {
        if (object == null || !object.getClass().isArray()) {
            return Optional.empty();
        }

        Class<?> type = object.getClass();
        int arrayLength = Array.getLength(object);

        JsonObject adapted = new JsonObject();
        adapted.set("meta.adapter", this.getClass().getCanonicalName());
        adapted.set("meta.length", arrayLength);
        adapted.set("meta.component.type", type.getComponentType().getCanonicalName());

        Class<?> componentType = type.getComponentType();
        int dimensions = 0;
        while (componentType.isArray()) {
            dimensions += 1;
            componentType = componentType.getComponentType();

        }
        adapted.set("meta.component.dimensions", dimensions);

        JsonArray values = new JsonArray();
        for (int i = 0; i < arrayLength; i++) {
            Object element = Array.get(object, i);
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
        if (object == null || !object.getClass().isArray()) {
            return Optional.empty();
        }

        int arrayLength = Array.getLength(object);

        JsonArray adapted = new JsonArray();
        for (int i = 0; i < arrayLength; i++) {
            Object value = Array.get(object, i);
            adapted.add(ObjectScan.represent(value));
        }

        return Optional.of(adapted);
    }

    /**
     * Resolve the JSON value
     * into the java type
     *
     * @param jsonElement the JSON type
     * @return the java type
     */
    @Override @SuppressWarnings("unchecked")
    public @NotNull Optional<T> resolve(final @NotNull JsonElement jsonElement) {
        JsonObject object = jsonElement.object();

        JsonObject component = object.get("meta.component").object();
        JsonArray values = object.get("values").array();

        try {
            Class<?> componentType = Class.forName(component.get("type").asString());
            int dimensions = component.get("dimensions").asNumber().intValue();
            while (dimensions-- > 1) {
                componentType = componentType.arrayType();
            }

            Object array = Array.newInstance(componentType, object.get("meta.length").asNumber().intValue());
            int index = 0;
            for (JsonElement element : values) {
                if (element.isNull()) {
                    Array.set(array, index++, null);
                    continue;
                }

                Object loaded = ObjectScan.deserialize(element);
                Array.set(array, index++, loaded);
            }

            return Optional.of((T) array);
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
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
