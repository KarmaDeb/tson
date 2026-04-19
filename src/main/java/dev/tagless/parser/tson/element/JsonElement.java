package dev.tagless.parser.tson.element;

import dev.tagless.parser.tson.serializer.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The JSON element represents an element
 * in a JSON document
 */
@SuppressWarnings("unused")
public abstract class JsonElement {

    protected String key;
    protected JsonElement parent;

    /**
     * Get the element key
     * @return the key of the element
     */
    @NotNull
    public abstract String getKey();

    /**
     * Get the route to this element
     * @return the route to the element
     */
    @NotNull
    public final String getRoute() {
        List<String> paths = new ArrayList<>();
        List<String> indexes = new ArrayList<>();

        JsonElement element = this;

        while (element != null) {
            if (element.parent != null && element.parent.isArray()) {
                indexes.add(element.key);
            } else {
                paths.addFirst(String.format("%s%s", element.key, String.join("", indexes)));
                // indexes is already a list of strings in format [%d]
                indexes.clear();
            }

            element = element.parent;
        }

        return String.join(".", paths);
    }

    /**
     * Get the element parent
     * @return the parent element
     */
    @NotNull
    public abstract Optional<JsonElement> getParent();

    /**
     * Get if the element is a JSON primitive
     * @return if the element is primitive
     */
    public abstract boolean isPrimitive();

    /**
     * Get if the element is a JSON object
     * @return if the element is an object
     */
    public abstract boolean isObject();

    /**
     * Get if the element is a JSON array
     * @return if the element is an array
     */
    public abstract boolean isArray();

    /**
     * Get if the element is a JSON primitive
     * null
     * @return if the element is null
     */
    public boolean isNull() {
        return false;
    }

    /**
     * Get if the element is a JSON primitive
     * boolean
     * @return if the element is null
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * Get if the element is a JSON primitive
     * number
     * @return if the element is number
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * Get if the element is a JSON primitive
     * string
     * @return if the element is string
     */
    public boolean isString() {
        return false;
    }

    /**
     * Get the current element as a JSON
     * object
     * @return the element
     */
    @NotNull
    public Optional<JsonObject> getAsObject() {
        if (!(this instanceof JsonObject object)) {
            return Optional.empty();
        }

        return Optional.of(object);
    }

    /**
     * Get the current element as a JSON
     * array
     * @return the element
     */
    @NotNull
    public Optional<JsonArray> getAsArray() {
        if (!(this instanceof JsonArray object)) {
            return Optional.empty();
        }

        return Optional.of(object);
    }

    /**
     * Get the current element as a JSON
     * primitive
     * @return the element
     */
    @NotNull
    public Optional<JsonPrimitive> getAsPrimitive() {
        if (!(this instanceof JsonPrimitive object)) {
            return Optional.empty();
        }

        return Optional.of(object);
    }


    /**
     * Get the element as a string if it's
     * a JSON primitive
     * @return the string value
     */
    public String asString() {
        return this.getAsPrimitive().map(
                JsonPrimitive::getAsString
        ).orElse(null);
    }

    /**
     * Get the element as a number if it's
     * a JSON primitive
     * @return the number value
     */
    public Number asNumber() {
        return this.getAsPrimitive().map(
                JsonPrimitive::getAsNumber
        ).orElse(null);
    }

    /**
     * Get the element as a boolean if it's
     * a JSON primitive
     * @return the boolean value
     */
    public Boolean asBoolean() {
        return this.getAsPrimitive().map(
                JsonPrimitive::getAsBoolean
        ).orElse(null);
    }

    /**
     * Get the element as a JSON object if it's
     * a JSON object
     * @return the object value
     */
    public JsonObject object() {
        if (this.isObject()) {
            return (JsonObject) this;
        }

        return null;
    }

    /**
     * Get the element as a JSON array if it's
     * a JSON array
     * @return the array value
     */
    public JsonArray array() {
        if (this.isArray()) {
            return (JsonArray) this;
        }

        return null;
    }

    /**
     * Get the element as a JSON primitive if it's
     * a JSON primitive
     * @return the primitive value
     */
    public JsonPrimitive primitive() {
        if (this.isPrimitive()) {
            return (JsonPrimitive) this;
        }

        return null;
    }

    /**
     * Replace this element with the provided one
     * @param element the element to replace with
     */
    protected final void replaceWith(final @NotNull JsonElement element) {
        if (this.parent == null) {
            element.key = this.key;
            return;
        }

        if (this.parent.isArray()) {
            JsonArray array = (JsonArray) this.parent;
            int index = array.indexOf(this);

            array.replace(index, element);
        } else if (this.parent.isObject()) {
            JsonObject object = (JsonObject) this.parent;
            object.set(this.key, element);
        }

        this.key = "$.";
        this.parent = null;
    }

    /**
     * Transfer the element key and parent
     * @param key the new key
     * @param destination the new parent
     */
    protected final void transfer(final @NotNull String key, final JsonElement destination) {
        if (this.parent != null) {
            if (this.parent.isObject()) {
                JsonObject o = (JsonObject) this.parent;
                o.unset(this.key);
            }
            if (this.parent.isArray()) {
                JsonArray o = (JsonArray) this.parent;
                o.remove(this);
            }
        }

        this.key = key;
        this.parent = destination;
    }

    /**
     * {@return a string representation of the object}
     * <p>
     * Satisfying this method's contract implies a non-{@code null}
     * result must be returned.
     *
     * @apiNote In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * The string output is not necessarily stable over time or across
     * JVM invocations.
     * @implSpec The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * {@snippet lang = java:
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     *}
     * The {@link Objects#toIdentityString(Object)
     * Objects.toIdentityString} method returns the string for an
     * object equal to the string that would be returned if neither
     * the {@code toString} nor {@code hashCode} methods were
     * overridden by the object's class.
     */
    @Override
    public String toString() {
        try {
            return JsonWriter.writer(this).write();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}