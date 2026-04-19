package dev.tagless.parser.tson.element;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * JSON array
 */
@SuppressWarnings("unused")
public final class JsonArray extends JsonElement implements Iterable<JsonElement> {

    private final LinkedList<JsonElement> elements;

    /**
     * Create a new object
     */
    public JsonArray() {
        this.key = "$";
        this.elements = new LinkedList<>();
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
        return false;
    }

    /**
     * Get if the element is a JSON array
     *
     * @return if the element is an array
     */
    @Override
    public boolean isArray() {
        return true;
    }

    @ApiStatus.Internal
    public Optional<JsonElement> getDirect(int index) {
        if (index >= 0 && index < this.elements.size()) {
            return Optional.of(this.elements.get(index));
        }
        return Optional.empty();
    }

    @ApiStatus.Internal
    public void setDirect(int index, JsonElement element) {
        while (this.elements.size() <= index) {
            this.elements.add(new JsonPrimitive());
        }
        element.transfer(String.format("[%d]", index), this);
        this.elements.set(index, element);
    }

    @ApiStatus.Internal
    public boolean removeDirect(int index) {
        if (index >= 0 && index < this.elements.size()) {
            this.elements.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Get if the array has the element
     * @param element the element
     * @return if the array has the element
     */
    public boolean has(final @NotNull JsonElement element) {
        return this.elements.contains(element);
    }

    /**
     * Get if the array has the element
     * @param property the element
     * @return if the array has the element
     */
    public boolean has(final @NotNull String property) {
        return this.elements.stream().filter(JsonElement::isPrimitive)
                .map(JsonPrimitive.class::cast)
                .filter(JsonPrimitive::isString)
                .map(JsonPrimitive::getString)
                .anyMatch(property::equals);
    }

    /**
     * Get if the array has the element
     * @param property the element
     * @return if the array has the element
     */
    public boolean has(final @NotNull Boolean property) {
        return this.elements.stream().filter(JsonElement::isPrimitive)
                .map(JsonPrimitive.class::cast)
                .filter(JsonPrimitive::isBoolean)
                .map(JsonPrimitive::getBoolean)
                .anyMatch(property::equals);
    }

    /**
     * Get if the array has the element
     * @param property the element
     * @return if the array has the element
     */
    public boolean has(final @NotNull Number property) {
        return this.elements.stream().filter(JsonElement::isPrimitive)
                .map(JsonPrimitive.class::cast)
                .filter(JsonPrimitive::isNumber)
                .map(JsonPrimitive::getNumber)
                .anyMatch(num -> {
                    if (num instanceof BigInteger in) {
                        if (property.equals(in)) {
                            return true;
                        }

                        BigInteger bi = new BigInteger(String.valueOf(property));
                        return bi.equals(in);
                    } else if (num instanceof BigDecimal de) {
                        if (property.equals(de)) {
                            return true;
                        }

                        BigDecimal bi = new BigDecimal(String.valueOf(property));
                        return bi.equals(de);
                    } else if (num instanceof Float || num instanceof Double) {
                        return property.doubleValue() == num.doubleValue();
                    } else {
                        return property.longValue() == num.longValue();
                    }
                });
    }

    /**
     * Get if the array has the null element
     * @return if the array has the null element
     */
    public boolean hasNull() {
        return this.elements.stream().filter(JsonElement::isPrimitive)
                .map(JsonPrimitive.class::cast)
                .anyMatch(JsonPrimitive::isNull);
    }

    /**
     * Get the index of an element, or -1 if the
     * element is not present
     * @param element the element
     * @return the element index
     */
    public int indexOf(final @NotNull JsonElement element) {
        return this.elements.indexOf(element);
    }

    /**
     * Get the index of an element using the specified
     * offset as "skip n".
     * @param element the element
     * @param offset the amount of elements to
     *               skip
     * @return the element index
     */
    public int indexOf(final @NotNull JsonElement element, final int offset) {
        if (offset >= this.elements.size()) {
            return -1;
        }

        List<JsonElement> sub = this.elements.subList(offset, this.elements.size());
        return sub.indexOf(element) + offset;
    }

    /**
     * Get the right index of an element, or -1 if the
     * element is not present
     * @param element the element
     * @return the element index
     */
    public int lastIndexOf(final @NotNull JsonElement element) {
        return this.elements.lastIndexOf(element);
    }

    /**
     * Get the right index of an element using the specified
     * offset as "skip n".
     * @param element the element
     * @param offset the amount of elements to
     *               skip
     * @return the element index
     */
    public int lastIndexOf(final @NotNull JsonElement element, final int offset) {
        if (offset >= this.elements.size()) {
            return -1;
        }

        List<JsonElement> sub = this.elements.subList(offset, this.elements.size());
        return sub.lastIndexOf(element) + offset;
    }

    /**
     * Get an element
     * @param index the element index
     * @return the element
     */
    public JsonElement get(final int index) {
        if (index < 0 || index >= this.elements.size()) {
            return null;
        }

        return this.elements.get(index);
    }

    /**
     * Finds an element
     * @param index the element index
     * @return the element or an empty
     * optional if the element was not found
     */
    public Optional<JsonElement> find(final int index) {
        return Optional.ofNullable(this.get(index));
    }

    /**
     * Set a property
     * @param value the property value
     * @return if the element was inserted
     */
    public boolean add(final String value) {
        JsonPrimitive primitive = new JsonPrimitive(value);
        return this.add(primitive);
    }

    /**
     * Set a property
     * @param number the property value
     * @return if the element was inserted
     */
    public boolean add(final Number number) {
        JsonPrimitive primitive = new JsonPrimitive(number);
        return this.add(primitive);
    }

    /**
     * Set a property
     * @param bool the property value
     * @return if the element was inserted
     */
    public boolean add(final Boolean bool) {
        JsonPrimitive primitive = new JsonPrimitive(bool);
        return this.add(primitive);
    }

    /**
     * Set a property as null
     * @return if the element was inserted
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean addNull() {
        return this.add(new JsonPrimitive());
    }

    /**
     * Set a property
     * @param object the property
     * @return if the element was inserted
     */
    public boolean add(final @NotNull JsonElement object) {
        return this.insert(this.elements.size(), object);
    }

    /**
     * Set a property
     * @param index the index in where to insert
     *              the property
     * @param value the property value
     * @return if the element was inserted
     */
    public boolean insert(final int index, final String value) {
        JsonElement element = new JsonPrimitive(value);
        return this.insert(index, element);
    }

    /**
     * Set a property
     * @param index the index in where to insert
     *              the property
     * @param number the property value
     * @return if the element was inserted
     */
    public boolean insert(final int index, final Number number) {
        JsonElement element = new JsonPrimitive(number);
        return this.insert(index, element);
    }

    /**
     * Set a property
     * @param index the index in where to insert
     *              the property
     * @param bool the property value
     * @return if the element was inserted
     */
    public boolean insert(final int index, final Boolean bool) {
        JsonPrimitive element = new JsonPrimitive(bool);
        return this.insert(index, element);
    }

    /**
     * Set a property as null
     * @param index the index in where to insert
     *              the property
     * @return if the element was inserted
     */
    public boolean insertNull(final int index) {
        JsonElement element = new JsonPrimitive();
        return this.insert(index, element);
    }

    /**
     * Adds a property
     * @param index the index in where to insert
     *              the property
     * @param element the property primitive
     * @return if the element was inserted
     */
    public boolean insert(final int index, final @NotNull JsonElement element) {
        if (index < 0) {
            return false;
        }

        if (index > this.elements.size()) {
            for (int i = 0; i < index - this.elements.size(); i++) {
                JsonPrimitive primitive = new JsonPrimitive();
                primitive.transfer(String.format("[%d]", this.elements.size()), this);
                this.elements.add(primitive);
            }
        }

        this.elements.add(index, element);
        for (int i = index + 1; i < this.elements.size(); i++) {
            JsonElement indexElement = this.elements.get(i);
            indexElement.transfer(String.format("[%d]", i), this);
        }

        element.transfer(String.format("[%d]", index), this);
        return true;
    }

    /**
     * Set a property
     * @param index the index in where to insert
     *              the property
     * @param value the property value
     * @return if the element was inserted
     */
    public boolean replace(final int index, final String value) {
        JsonElement element = new JsonPrimitive(value);
        return this.replace(index, element);
    }

    /**
     * Set a property
     * @param index the index in where to insert
     *              the property
     * @param number the property value
     * @return if the element was inserted
     */
    public boolean replace(final int index, final Number number) {
        JsonElement element = new JsonPrimitive(number);
        return this.replace(index, element);
    }

    /**
     * Set a property
     * @param index the index in where to insert
     *              the property
     * @param bool the property value
     * @return if the element was inserted
     */
    public boolean replace(final int index, final Boolean bool) {
        JsonPrimitive element = new JsonPrimitive(bool);
        return this.replace(index, element);
    }

    /**
     * Set a property as null
     * @param index the index in where to insert
     *              the property
     * @return if the element was inserted
     */
    public boolean replace(final int index) {
        JsonElement element = new JsonPrimitive();
        return this.replace(index, element);
    }

    /**
     * Adds a property
     * @param index the index in where to insert
     *              the property
     * @param element the property primitive
     * @return if the element was inserted
     */
    public boolean replace(final int index, final @NotNull JsonElement element) {
        if (index < 0) {
            return false;
        }
        if (index == this.elements.size()) {
            JsonPrimitive primitive = new JsonPrimitive();
            this.elements.add(primitive); // Dummy
        }

        if (index > this.elements.size()) {
            for (int i = 0; i < index - this.elements.size(); i++) {
                JsonPrimitive primitive = new JsonPrimitive();
                primitive.transfer(String.format("[%d]", this.elements.size()), this);
                this.elements.add(primitive);
            }
        }

        this.elements.set(index, element);
        element.transfer(String.format("[%d]", index), this);
        return true;
    }

    /**
     * Removes a property
     * @param element the property
     * @return if the element was removed
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean remove(final @NotNull JsonElement element) {
        if (this.elements.remove(element)) {
            Optional<JsonElement> parent = element.getParent();
            if (parent.isEmpty()) {
                return true;
            }

            if (parent.get().equals(this)) {
                element.transfer("", null);
            }

            int index = 0;
            for (JsonElement item : this) {
                item.transfer(String.format("[%d]", index++), this);
            }

            return true;
        }

        return false;
    }

    /**
     * Removes a property
     * @param index the property index
     * @return if the element was removed
     */
    public boolean remove(final int index) {
        if (index < 0 || index >= this.elements.size()) {
            return false;
        }

        JsonElement element = this.elements.get(index);
        this.remove(element);

        return true;
    }

    /**
     * Get the JSON array size
     * @return the size of the array
     */
    public int size() {
        return this.elements.size();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public @NotNull Iterator<JsonElement> iterator() {
        return Collections.unmodifiableCollection(this.elements).iterator();
    }
}
