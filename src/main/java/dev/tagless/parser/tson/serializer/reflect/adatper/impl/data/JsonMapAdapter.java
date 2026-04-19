package dev.tagless.parser.tson.serializer.reflect.adatper.impl.data;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import dev.tagless.parser.tson.serializer.reflect.scan.ObjectScan;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * map element adapter for JSON
 */
public final class JsonMapAdapter<K, V> implements JsonTypeAdapter<Map<K, V>, JsonObject> {

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

        return type instanceof Map<?,?>;
    }

    /**
     * Adapt the java type
     *
     * @param object the type
     * @return the adapted java type
     */
    @Override
    public @NotNull Optional<JsonObject> adapt(final Object object) {
        if (!(object instanceof Map<?, ?> m)) {
            return Optional.empty();
        }

        Class<?> type = m.getClass();

        JsonObject adapted = new JsonObject();
        adapted.set("meta.adapter", this.getClass().getCanonicalName());
        adapted.set("meta.type", type.getCanonicalName());

        JsonArray values = new JsonArray();
        for (Map.Entry<?, ?> entry : m.entrySet()) {
            JsonObject entryObj = new JsonObject();

            Object key = entry.getKey();
            Object element = entry.getValue();

            JsonElement keySerialized = ObjectScan.serialize(key);
            JsonElement serialized = ObjectScan.serialize(element);

            entryObj.set("key", keySerialized);
            entryObj.set("value", serialized);

            values.add(entryObj);
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
        if (!(object instanceof Map<?, ?> m)) {
            return Optional.empty();
        }

        JsonArray array = new JsonArray();
        for (Map.Entry<?, ?> entry : m.entrySet()) {
            Object key = entry.getKey();
            Object element = entry.getValue();

            JsonElement keySerialized = ObjectScan.serialize(key);
            JsonElement serialized = ObjectScan.serialize(element);

            JsonObject o = new JsonObject();
            o.set("key", keySerialized);
            o.set("value", serialized);

            array.add(o);
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
    public @NotNull Optional<Map<K, V>> resolve(final @NotNull JsonElement jsonElement) {
        JsonObject object = jsonElement.object();

        JsonArray values = object.get("values").array();
        try {
            // noinspection rawtypes
            Class<? extends Map> componentType = Class.forName(object.get("meta.type").asString())
                    .asSubclass(Map.class);
            Map<Object, Object> map = componentType.getConstructor().newInstance();

            for (JsonElement element : values) {
                JsonObject o = element.object();

                JsonElement key = o.get("key");
                JsonElement value = o.get("value");

                Object keyLoaded = ObjectScan.deserialize(key);
                Object loaded = ObjectScan.deserialize(value);

                map.put(keyLoaded, loaded);
            }

            return Optional.of((Map<K, V>) map);
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
