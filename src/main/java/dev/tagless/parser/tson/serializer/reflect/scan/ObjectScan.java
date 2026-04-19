package dev.tagless.parser.tson.serializer.reflect.scan;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.serializer.reflect.JsonExclude;
import dev.tagless.parser.tson.serializer.reflect.JsonInclude;
import dev.tagless.parser.tson.serializer.reflect.JsonSerializable;
import dev.tagless.parser.tson.serializer.reflect.adatper.impl.JsonUUIDAdapter;
import dev.tagless.parser.tson.serializer.reflect.adatper.impl.data.JsonArrayAdapter;
import dev.tagless.parser.tson.serializer.reflect.adatper.impl.data.JsonCollectionAdapter;
import dev.tagless.parser.tson.serializer.reflect.adatper.impl.data.JsonMapAdapter;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import dev.tagless.parser.tson.serializer.reflect.adatper.impl.primitive.JsonBooleanAdapter;
import dev.tagless.parser.tson.serializer.reflect.adatper.impl.primitive.JsonCharAdapter;
import dev.tagless.parser.tson.serializer.reflect.adatper.impl.primitive.JsonCharSeqAdapter;
import dev.tagless.parser.tson.serializer.reflect.adatper.impl.primitive.JsonNumberAdapter;
import dev.tagless.parser.tson.serializer.reflect.constructor.JsonConstructor;
import dev.tagless.parser.tson.serializer.reflect.constructor.JsonField;
import dev.tagless.parser.tson.serializer.reflect.constructor.JsonSetter;
import dev.tagless.parser.tson.serializer.reflect.scan.exception.NotSerializableException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.time.Instant;
import java.util.*;

/**
 * The object scan reads a java instance to extract
 * the JSON serializable elements. The ObjectScan behaves
 * internally like a {@link JsonTypeAdapter} which knows
 * how to serialize any class annotated by {@link JsonSerializable}.
 */
@SuppressWarnings("unused")
public final class ObjectScan {

    private static final Collection<JsonTypeAdapter<?, ? extends JsonElement>> ADAPTERS = new HashSet<>(){
        {
            this.add(new JsonCharSeqAdapter());
            this.add(new JsonCharAdapter());
            this.add(new JsonBooleanAdapter());
            this.add(new JsonNumberAdapter());
            this.add(new JsonArrayAdapter<>());
            this.add(new JsonCollectionAdapter<>());
            this.add(new JsonMapAdapter<>());
            this.add(new JsonUUIDAdapter());
        }
    };

    /**
     * Adds a global adapter
     * @param adapter the adapter
     */
    public static void registerAdapter(final JsonTypeAdapter<?, ? extends JsonElement> adapter) {
        ADAPTERS.add(adapter);
    }

    /**
     * Similar to {@link #serialize(Object)} but generates a human-friendly
     * JSON object which the object scan does not know how to revert
     * @param instance the object to represent
     * @return the object representation
     * @throws NotSerializableException if the object scanner does not
     * know how to serialize the object
     */
    public static JsonElement represent(final Object instance) throws NotSerializableException {
        if (instance == null) {
            return new JsonPrimitive();
        }

        Class<?> type = instance.getClass();
        if (type.isAnonymousClass() || type.isInterface()) {
            throw new NotSerializableException(type);
        }

        if (!type.isAnnotationPresent(JsonSerializable.class)) {
            for (var adapter : ADAPTERS) {
                if (adapter.subTest(instance)) {
                    Optional<? extends JsonElement> element = adapter.represent(instance);
                    if (element.isPresent()) {
                        return element.get();
                    }
                }
            }

            throw new NotSerializableException(instance);
        }

        JsonSerializable serializable = type.getAnnotation(JsonSerializable.class);
        ScanContext context = ObjectScan.scan(type, serializable);

        return ObjectScan.applyRepresentation(context, instance, serializable);
    }

    @NotNull
    private static JsonObject applyRepresentation(final @NotNull ScanContext context,
                                                  final @NotNull Object instance,
                                                  final @NotNull JsonSerializable serializable) {
        JsonObject root = new JsonObject();

        JsonObject processed = context.process(instance, true);
        JsonArray meta = processed.getArray("meta");
        assert meta != null;

        for (JsonElement element : meta) {
            assert element instanceof JsonObject;
            JsonObject object = (JsonObject) element;

            String name = object.get("name").getAsPrimitive()
                    .orElseThrow().getAsString();
            JsonElement value = object.get("value");

            root.set(name, value);
        }

        ScanContext upper = context.upper();
        if (upper != null) {
            JsonSerializable s = serializable;
            if (upper.clazz().isAnnotationPresent(JsonSerializable.class)) {
                s = upper.clazz().getAnnotation(JsonSerializable.class);
            }

            JsonObject result = applyRepresentation(upper, instance, s);
            String name = upper.clazz().isAnnotationPresent(JsonSerializable.class) && !s.name().isBlank() ?
                    upper.clazz().getSimpleName()
                    .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                    .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                    .toLowerCase() :
                    s.name();

            root.set(name, result);
        }

        return root;
    }

    /**
     * Serialize an element
     * @return the element to serialize
     * @throws NotSerializableException if the object scanner does not
     * know how to serialize the object
     */
    @NotNull
    public static JsonElement serialize(final Object object) throws NotSerializableException {
        if (object == null) {
            return new JsonPrimitive();
        }

        Class<?> objectType = object.getClass();
        if (objectType.isAnonymousClass() || objectType.isInterface()) {
            throw new NotSerializableException(objectType);
        }

        if (!objectType.isAnnotationPresent(JsonSerializable.class)) {
            for (var adapter : ADAPTERS) {
                if (adapter.subTest(object)) {
                    // noinspection unchecked
                    Optional<JsonElement> element = ((JsonTypeAdapter<Object, JsonElement>) adapter).adapt(object);
                    if (element.isPresent()) {
                        return element.get();
                    }
                }
            }

            throw new NotSerializableException(object);
        }

        return ObjectScan.toJsonObject(objectType, object);
    }

    /**
     * Loads a serialized element
     * @param element the element to load
     * @return the loaded element
     * @param <T> the element type
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> deserialize(final @NotNull JsonElement element) {
        for (var adapter : ADAPTERS) {
            if (adapter.test(element)) {
                return (Optional<T>) adapter.resolve(element);
            }
        }

        if (element.isObject()) {
            JsonObject object = element.object();
            if (object.has("class") &&
                    object.has("version") &&
                    object.has("fields")) {

                try {
                    T instance = ObjectScan.load(object);
                    return Optional.of(instance);
                } catch (NoClassDefFoundError | ReflectiveOperationException ex) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static <T> T load(final JsonObject object) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName(object.get("class").asString());
        long version = object.get("version").asNumber().longValue();
        JsonArray fields = object.get("fields").array();

        Object useConstructor = null;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (!constructor.isAnnotationPresent(JsonConstructor.class)) {
                continue;
            }

            long constructorVersion = constructor.getAnnotation(JsonConstructor.class).value();
            if (constructorVersion == version) {
                useConstructor = constructor;
                break;
            }
        }
        if (useConstructor == null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) || !method.isAnnotationPresent(JsonConstructor.class)) {
                    continue;
                }

                JsonConstructor constructor = method.getAnnotation(JsonConstructor.class);
                if (constructor.value() == version) {
                    useConstructor = method;
                    break;
                }
            }
        }

        Object instance = null;
        if (useConstructor == null) {
            Constructor<?> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);

            instance = c.newInstance();
            for (Method method : clazz.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || !method.isAnnotationPresent(JsonConstructor.class)) {
                    continue;
                }

                JsonConstructor constructor = method.getAnnotation(JsonConstructor.class);
                if (constructor.value() == version) {
                    useConstructor = method;
                    break;
                }
            }
        }

        if (useConstructor != null) {
            Map<String, JsonElement> fieldMap = new LinkedHashMap<>();
            for (JsonElement e : fields) {
                JsonObject obj = e.object();
                JsonArray meta = obj.get("meta").array();
                for (JsonElement mE : meta) {
                    JsonObject mObj = mE.object();

                    String name = mObj.get("name").asString();
                    if (fieldMap.containsKey(name)) {
                        continue;
                    }

                    fieldMap.put(name, mObj.get("value"));
                }
            }

            Parameter[] parameters;
            if (useConstructor instanceof Method m) {
                parameters = m.getParameters();
            } else {
                Constructor<?> constructor = (Constructor<?>) useConstructor;
                parameters = constructor.getParameters();
            }

            Object[] values = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (!parameter.isAnnotationPresent(JsonField.class)) {
                    throw new IllegalStateException("""
                                    Cannot deserialize because json marked constructor \
                                    is missing a field declaration""");
                }

                JsonField field = parameter.getAnnotation(JsonField.class);

                String name = field.value();
                JsonElement value = fieldMap.get(name);
                if (value == null || value.isNull()) {
                    values[i] = null;
                    continue;
                }

                values[i] = ObjectScan.deserialize(value).orElse(null);
            }

            if (useConstructor instanceof Constructor<?> c) {
                instance = c.newInstance(values);
            } else {
                if (instance == null) {
                    Constructor<?> c = clazz.getDeclaredConstructor();
                    c.setAccessible(true);

                    instance = c.newInstance();
                }

                Method method = (Method) useConstructor;
                method.invoke(instance, values);
            }
        }

        ObjectScan.mapInstanceFields(instance, fields);
        return (T) instance;
    }

    private static void mapInstanceFields(final Object instance, final JsonArray fields) {
        Collection<Method> allMethods = new LinkedList<>();
        Collection<Field> allFields = new LinkedList<>();

        Class<?> type = instance.getClass();
        while (type != null && !type.equals(Object.class)) {
            for (Method m : type.getDeclaredMethods()) {
                if (!m.isAnnotationPresent(JsonSetter.class)) {
                    continue;
                }

                allMethods.add(m);
            }
            for (Field f : type.getDeclaredFields()) {
                if (f.isAnnotationPresent(JsonExclude.class)) {
                    continue;
                }

                allFields.add(f);
            }

            type = type.getSuperclass();
        }

        for (JsonElement e : fields) {
            JsonObject obj = e.object();
            try {
                Class<?> clazz = Class.forName(obj.get("class").asString());
                JsonArray meta = obj.get("meta").array();
                field:
                for (JsonElement mE : meta) {
                    JsonObject mObj = mE.object();

                    String name = mObj.get("name").asString();
                    JsonElement value = mObj.get("value");

                    try {
                        for (Method setter : allMethods) {
                            if (!setter.getDeclaringClass().equals(clazz)) {
                                continue;
                            }

                            JsonSetter s = setter.getAnnotation(JsonSetter.class);
                            if (s.value().equals(name)) {
                                if (s.ifNotNull() && value.isNull()) {
                                    continue;
                                }

                                setter.setAccessible(true);
                                setter.invoke(instance, ObjectScan.deserialize(value).orElse(null));
                                continue field;
                            }
                        }

                        for (Field f : allFields) {
                            if (!f.getDeclaringClass().equals(clazz)) {
                                continue;
                            }

                            String fName = f.getName();
                            if (f.isAnnotationPresent(JsonInclude.class)) {
                                fName = f.getAnnotation(JsonInclude.class).value();
                                if (fName.isBlank()) {
                                    fName = f.getName();
                                }
                            }

                            if (fName.equals(name)) {
                                f.setAccessible(true);
                                f.set(instance, ObjectScan.deserialize(value).orElse(null));
                                continue field;
                            }
                        }
                    } catch (ReflectiveOperationException ignored) {}
                }
            } catch (ClassNotFoundException ignored) {}
        }
    }

    private static JsonObject toJsonObject(final @NotNull Class<?> type, final @NotNull Object instance) {
        JsonSerializable serializable = type.getAnnotation(JsonSerializable.class);

        JsonObject object = new JsonObject();
        object.set("class", type.getCanonicalName());
        object.set("version", serializable.value());
        object.set("date", Instant.now().toEpochMilli());

        ScanContext context = scan(type, serializable);
        JsonArray fields = new JsonArray();

        object.set("fields", fields);

        while (context != null) {
            JsonObject processed = context.process(instance, false);
            fields.add(processed);

            context = context.upper();
        }

        return object;
    }

    private static ScanContext scan(final @NotNull Class<?> type, final JsonSerializable settings) {
        if (type.isAnnotationPresent(JsonExclude.class)) {
            return new ScanContext(type, new Field[0], new Method[0], null);
        }

        Collection<Field> fields = new LinkedHashSet<>();
        Collection<Method> methods = new LinkedList<>();
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonExclude.class)) {
                continue;
            }

            if (settings.includeByDefault() || field.isAnnotationPresent(JsonInclude.class)) {
                fields.add(field);
            }
        }

        for (Method method : type.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(JsonInclude.class)) {
                continue;
            }

            methods.add(method);
        }

        if (settings.deepScan()) {
            Class<?> s = type.getSuperclass();
            if (s == null || s.equals(Object.class) || s.isAnnotationPresent(JsonExclude.class)) {
                return new ScanContext(
                        type,
                        fields.toArray(new Field[0]),
                        methods.toArray(new Method[0]),
                        null
                );
            }

            JsonSerializable superSettings = settings;
            if (s.isAnnotationPresent(JsonSerializable.class)) {
                superSettings = s.getAnnotation(JsonSerializable.class);
            }

            ScanContext sup = scan(s, superSettings);
            return new ScanContext(
                    type,
                    fields.toArray(new Field[0]),
                    methods.toArray(new Method[0]),
                    sup
            );
        }

        return new ScanContext(
                type,
                fields.toArray(new Field[0]),
                methods.toArray(new Method[0]),
                null
        );
    }
}
