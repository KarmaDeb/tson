package dev.tagless.parser.tson.serializer.reflect.scan;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.serializer.reflect.JsonInclude;
import dev.tagless.parser.tson.serializer.reflect.adatper.JsonTypeAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * The scan context is a graph-like object which
 * contains all suitable JSON serialization methods
 * and fields
 */
public record ScanContext(
        @NotNull Class<?> clazz,
        Field @NotNull[] fields,
        Method @NotNull[] methods,
        ScanContext upper) {

    public JsonObject process(final @NotNull Object instance,
                              final boolean represent) {
        JsonObject object = new JsonObject();
        JsonArray meta = new JsonArray();

        int max = fields.length + methods.length;
        object.set("length", max);
        object.set("class", this.clazz.getCanonicalName());
        object.set("meta", meta);

        for (int i = 0; i < max; i++) {
            int target = i;
            Object use;
            if (target >= fields.length) {
                target = i - fields.length;
                use = methods[target];
            } else {
                use = fields[target];
            }

            JsonObject field = new JsonObject();

            String name = null;
            Object[] adapters = new Class[0];
            Object tmpValue = null;
            try {
                if (use instanceof Field f) {
                    JsonInclude include = f.getAnnotation(JsonInclude.class);
                    if (include == null) {
                        name = f.getName();
                    } else {
                        if (!include.value().isBlank()) {
                            name = include.value();
                        } else {
                            name = f.getName();
                        }
                        adapters = include.adapters();
                    }

                    f.setAccessible(true);
                    tmpValue = f.get(Modifier.isStatic(f.getModifiers()) ? this.clazz : instance);
                } else if (use instanceof Method m) {
                    JsonInclude include = m.getAnnotation(JsonInclude.class);
                    if (!include.value().isBlank()) {
                        name = include.value();
                    } else {
                        name = m.getName();
                    }
                    adapters = include.adapters();

                    m.setAccessible(true);
                    tmpValue = m.invoke(Modifier.isStatic(m.getModifiers()) ? this.clazz : instance);
                }

                JsonElement e = null;
                if (tmpValue instanceof JsonElement je) {
                    e = je;
                } else {
                    if (tmpValue == null) {
                        e = new JsonPrimitive();
                    } else {
                        for (var typeAdapters : adapters) {
                            // noinspection unchecked
                            Class<? extends JsonTypeAdapter<Object, JsonElement>> a = (Class<? extends JsonTypeAdapter<
                                    Object, JsonElement>>) typeAdapters;

                            try {
                                var adapter = a.getConstructor().newInstance();
                                if (!adapter.subTest(tmpValue)) {
                                    continue;
                                }

                                if (represent) {
                                    e = adapter.represent(tmpValue).orElse(null);
                                } else {
                                    e = adapter.adapt(tmpValue).orElse(null);
                                }

                                if (e != null) {
                                    break;
                                }
                            } catch (ReflectiveOperationException ignored) {}
                        }

                        if (e == null) {
                            e = ObjectScan.serialize(tmpValue);
                        }
                    }
                }

                field.set("name", name);
                field.set("value", e);
            } catch (ReflectiveOperationException ex) {
                field.set("name", name);
                field.setNull("value");
                field.set("error.message", ex.getMessage());

                JsonArray stackTrace = new JsonArray();
                StackTraceElement[] trace = ex.getStackTrace();
                for (StackTraceElement element : trace) {
                    stackTrace.add(element.toString());
                }

                field.set("error.stack", stackTrace);
            }

            meta.add(field);
        }

        return object;
    }
}
