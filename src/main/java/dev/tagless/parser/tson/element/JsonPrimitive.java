package dev.tagless.parser.tson.element;

import dev.tagless.parser.tson.FunctionChain;
import dev.tagless.parser.tson.element.exception.PrimitiveCastException;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * JSON primitive
 */
public final class JsonPrimitive extends JsonElement {

    private final Object value;

    /**
     * Create a new JSON primitive
     * @param value the primitive value
     */
    public JsonPrimitive(final String value) {
        this.value = value;
    }

    /**
     * Create a new JSON primitive
     * @param value the primitive value
     */
    public JsonPrimitive(final Boolean value) {
        this.value = value;
    }

    /**
     * Create a new JSON primitive
     * @param value the primitive value
     */
    public JsonPrimitive(final Number value) {
        this.value = value;
    }

    /**
     * Create a new JSON primitive
     */
    public JsonPrimitive() {
        this.value = null;
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
        return true;
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
        return false;
    }

    /**
     * Get the element as a string regardless
     * of its original type
     * @return the element as a string
     */
    public String getAsString() {
        if (this.value == null) {
            return null;
        }

        return String.valueOf(this.value);
    }

    /**
     * Get the element as a boolean regardless
     * of its original type
     * @return the element as a boolean
     */
    public Boolean getAsBoolean() {
        if (this.value == null) {
            return null;
        }

        String raw = String.valueOf(this.value);
        return raw.equalsIgnoreCase("true") ||
                raw.equals("1") ||
                raw.equalsIgnoreCase("t") ||
                raw.equals("y") ||
                raw.equalsIgnoreCase("on");
    }

    /**
     * Get the element as a number regardless
     * of its original type
     * @return the element as a number
     */
    public Number getAsNumber() {
        if (this.value == null) {
            return null;
        }

        try {
            return number(this.value);
        } catch (NumberFormatException ex) {
            if (this.value instanceof Boolean b) {
                return b ? 1 : 0;
            }

            return -1;
        }
    }

    /**
     * Get the element string value
     * @return the string value
     * @throws PrimitiveCastException if the variable cannot be cast
     */
    public String getString() {
        if (!(this.value instanceof String str)) {
            throw new PrimitiveCastException(this.value, "string");
        }

        return str;
    }

    /**
     * Get the element boolean value
     * @return the boolean value
     * @throws PrimitiveCastException if the variable cannot be cast
     */
    public boolean getBoolean() {
        if (!(this.value instanceof Boolean bool)) {
            throw new PrimitiveCastException(this.value, "boolean");
        }

        return bool;
    }

    /**
     * Get the element number value
     * @return the number value
     * @throws PrimitiveCastException if the variable cannot be cast
     */
    public Number getNumber() throws PrimitiveCastException {
        if (!(this.value instanceof Number num)) {
            throw new PrimitiveCastException(this.value, "number");
        }

        return num;
    }

    /**
     * Get if the element is a JSON primitive
     * null
     *
     * @return if the element is null
     */
    @Override
    public boolean isNull() {
        return this.value == null;
    }

    /**
     * Get if the element is a JSON primitive
     * boolean
     *
     * @return if the element is null
     */
    @Override
    public boolean isBoolean() {
        return this.value instanceof Boolean;
    }

    /**
     * Get if the element is a JSON primitive
     * number
     *
     * @return if the element is number
     */
    @Override
    public boolean isNumber() {
        return this.value instanceof Number;
    }

    /**
     * Get if the element is a JSON primitive
     * string
     *
     * @return if the element is string
     */
    @Override
    public boolean isString() {
        return this.value instanceof String;
    }

    /**
     * Transform any object into a number
     * @param value the value
     * @return the numeric value
     */
    public static Number number(final Object value) {
        return ((FunctionChain<String, Number>) Byte::parseByte)
                .except(NumberFormatException.class, Short::parseShort)
                .except(NumberFormatException.class, Integer::parseInt)
                .except(NumberFormatException.class, Long::parseLong)
                .except(NumberFormatException.class, Float::parseFloat)
                .except(NumberFormatException.class, Double::parseDouble)
                .except(NumberFormatException.class, BigInteger::new)
                .except(NumberFormatException.class, BigDecimal::new)
                .apply(String.valueOf(value));
    }
}

