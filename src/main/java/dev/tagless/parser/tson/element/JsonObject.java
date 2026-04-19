package dev.tagless.parser.tson.element;

import dev.tagless.parser.tson.element.helper.JsonKeyPair;
import dev.tagless.parser.tson.element.navigator.JsonPath;
import dev.tagless.parser.tson.element.navigator.path.exception.PathException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * JSON object
 */
@SuppressWarnings("unused")
public final class JsonObject extends JsonElement implements Iterable<JsonKeyPair> {

    private final Map<String, JsonElement> elementMap;

    /**
     * Create a new object
     */
    public JsonObject() {
        this.key = "$";
        this.elementMap = new LinkedHashMap<>();
    }

    /**
     * Get the element key
     *
     * @return the key of the element
     */
    @Override
    public @NotNull String getKey() {
        return this.key;
    }

    /**
     * Get the element parent
     *
     * @return the parent element
     */
    @Override
    public @NotNull Optional<JsonElement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    /**
     * Get if the element is a JSON primitive
     *
     * @return if the element is primitive
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Get if the element is a JSON object
     *
     * @return if the element is an object
     */
    @Override
    public boolean isObject() {
        return true;
    }

    /**
     * Get if the element is a JSON array
     *
     * @return if the element is an array
     */
    @Override
    public boolean isArray() {
        return false;
    }

    @ApiStatus.Internal
    public Optional<JsonElement> getDirect(String key) {
        return Optional.ofNullable(this.elementMap.get(key));
    }

    @ApiStatus.Internal
    public void setDirect(String key, JsonElement element) {
        element.transfer(key, this);
        this.elementMap.put(key, element);
    }

    @ApiStatus.Internal
    public boolean removeDirect(String key) {
        return this.elementMap.remove(key) != null;
    }

    /**
     * Get if the JSON object has the key
     * pair
     * @param keyPair the key pair
     * @return if the element contains the key pair
     */
    public boolean contains(final @NotNull JsonKeyPair keyPair) {
        JsonElement element = this.elementMap.get(keyPair.getKey());
        return keyPair.equals(element);
    }

    /**
     * Add an element
     * @param keyPair the key pair
     * @return if the element was added
     */
    public boolean add(final @NotNull JsonKeyPair keyPair) {
        if (this.contains(keyPair)) {
            return false;
        }

        this.set(keyPair.getKey(), keyPair.getValue());
        return true;
    }

    /**
     * Tries to remove an element
     * @param keyPair the key pair
     * @return if the element was removed
     */
    public boolean remove(final @NotNull JsonKeyPair keyPair) {
        if (this.contains(keyPair)) {
            this.unset(keyPair.getKey());
            return true;
        }

        return false;
    }

    /**
     * Get if the element has the key
     * @param key the key
     * @return if the element has the key
     */
    public boolean has(final String key) {
        try {
            JsonPath path = JsonPath.path(key);
            return path.resolve(this).isPresent();
        } catch (PathException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get an element
     * @param key the element key
     * @return the element
     */
    public JsonElement get(final String key) {
        try {
            JsonPath path = JsonPath.path(key);
            return path.resolve(this).orElse(new JsonPrimitive());
        } catch (PathException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get an element
     * @param key the element key
     * @return the element
     */
    public JsonObject getObject(final String key) {
        JsonElement element = this.get(key);
        if (element instanceof JsonObject o) {
            return o;
        }

        return null;
    }

    /**
     * Get an element
     * @param key the element key
     * @return the element
     */
    public JsonArray getArray(final String key) {
        JsonElement element = this.get(key);
        if (element instanceof JsonArray o) {
            return o;
        }

        return null;
    }

    /**
     * Get an element
     * @param key the element key
     * @return the element
     */
    public JsonPrimitive getPrimitive(final String key) {
        JsonElement element = this.get(key);
        if (element instanceof JsonPrimitive o) {
            return o;
        }

        return null;
    }

    /**
     * Finds an element
     * @param key the element key
     * @return the element or an empty
     * optional if the element was not found
     */
    public Optional<JsonElement> find(final String key) {
        try {
            JsonPath path = JsonPath.path(key);
            return path.resolve(this);
        } catch (PathException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Finds an element
     * @param key the element key
     * @return the element or an empty
     * optional if the element was not found
     */
    public Optional<JsonObject> findObject(final String key) {
         Optional<JsonElement> element = this.find(key);
         return element.filter(JsonObject.class::isInstance)
                 .map(JsonObject.class::cast);
    }

    /**
     * Finds an element
     * @param key the element key
     * @return the element or an empty
     * optional if the element was not found
     */
    public Optional<JsonArray> findArray(final String key) {
         Optional<JsonElement> element = this.find(key);
         return element.filter(JsonArray.class::isInstance)
                 .map(JsonArray.class::cast);
    }

    /**
     * Finds an element
     * @param key the element key
     * @return the element or an empty
     * optional if the element was not found
     */
    public Optional<JsonPrimitive> findPrimitive(final String key) {
         Optional<JsonElement> element = this.find(key);
         return element.filter(JsonPrimitive.class::isInstance)
                 .map(JsonPrimitive.class::cast);
    }

    /**
     * Set a property
     * @param property the property key
     * @param value the property value
     * @return if the element was set
     */
    public boolean set(final String property, final String value) {
        JsonPrimitive primitive = new JsonPrimitive(value);
        return this.set(property, primitive);
    }

    /**
     * Set a property
     * @param property the property key
     * @param number the property value
     * @return if the element was set
     */
    public boolean set(final String property, final Number number) {
        JsonPrimitive primitive = new JsonPrimitive(number);
        return this.set(property, primitive);
    }

    /**
     * Set a property
     * @param property the property key
     * @param bool the property value
     * @return if the element was set
     */
    public boolean set(final String property, final Boolean bool) {
        JsonPrimitive primitive = new JsonPrimitive(bool);
        return this.set(property, primitive);
    }

    /**
     * Set a property as null
     * @param property the property key
     * @return if the element was set
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean setNull(final String property) {
        JsonPrimitive primitive = new JsonPrimitive();
        return this.set(property, primitive);
    }

    /**
     * Set a property
     * @param property the property key
     * @param element the property
     * @return if the element was set
     */
    public boolean set(final String property, final JsonElement element) {
        try {
            JsonPath path = JsonPath.path(property).getHead();
            return path.set(this, true, element);
        } catch (PathException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Unset a property
     * @param property the property key
     * @return if the property was unset
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean unset(final String property) {
        try {
            JsonPath path = JsonPath.path(property);
            return path.delete(this);
        } catch (PathException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get all the element keys
     * @return the element keys
     */
    public Collection<String> getKeys() {
        return Collections.unmodifiableMap(this.elementMap).keySet();
    }

    /**
     * Get all the element values
     * @return the element values
     */
    public Collection<JsonElement> getValues() {
        return Collections.unmodifiableMap(this.elementMap).values();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public @NotNull Iterator<JsonKeyPair> iterator() {
        Set<JsonKeyPair> list = new LinkedHashSet<>();
        for (Map.Entry<String, JsonElement> entry : this.elementMap.entrySet()) {
            list.add(new JsonKeyPair(this, entry.getKey(), entry.getValue()));
        }

        return list.iterator();
    }
}