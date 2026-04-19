package dev.tagless.parser.tson.element.helper;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * The JSON key pair is a direct representation of
 * the JSON object [key]: [value].
 */
public class JsonKeyPair extends JsonElement {

    private final JsonElement value;

    /**
     * Initialize the JSON key pair
     * @param parent the JSON parent
     * @param key the JSON key
     * @param value the JSON value
     */
    public JsonKeyPair(final @NotNull JsonObject parent,
                       final @NotNull String key,
                       final @NotNull JsonElement value) {
        this.key = key;
        this.parent = parent;
        this.value = value;
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
     * Get the element value
     * @return the element
     */
    @NotNull
    public JsonElement getValue() {
        return this.value;
    }

    /**
     * Get the element parent
     *
     * @return the parent element
     */
    @Override
    public @NotNull Optional<JsonElement> getParent() {
        return Optional.of(this.parent);
    }

    /**
     * Get if the element is a JSON primitive
     *
     * @return if the element is primitive
     */
    @Override
    public boolean isPrimitive() {
        return this.value.isPrimitive();
    }

    /**
     * Get if the element is a JSON object
     *
     * @return if the element is an object
     */
    @Override
    public boolean isObject() {
        return this.value.isObject();
    }

    /**
     * Get if the element is a JSON array
     *
     * @return if the element is an array
     */
    @Override
    public boolean isArray() {
        return this.value.isArray();
    }

    /**
     * Get if the element is a JSON primitive
     * null
     *
     * @return if the element is null
     */
    @Override
    public boolean isNull() {
        return this.value.isNull();
    }

    /**
     * Get if the element is a JSON primitive
     * boolean
     *
     * @return if the element is null
     */
    @Override
    public boolean isBoolean() {
        return this.value.isBoolean();
    }

    /**
     * Get if the element is a JSON primitive
     * number
     *
     * @return if the element is number
     */
    @Override
    public boolean isNumber() {
        return this.value.isNumber();
    }

    /**
     * Get if the element is a JSON primitive
     * string
     *
     * @return if the element is string
     */
    @Override
    public boolean isString() {
        return this.value.isString();
    }

    /**
     * Get the current element as a JSON
     * object
     *
     * @return the element
     */
    @Override
    public @NotNull Optional<JsonObject> getAsObject() {
        return this.value.getAsObject();
    }

    /**
     * Get the current element as a JSON
     * array
     *
     * @return the element
     */
    @Override
    public @NotNull Optional<JsonArray> getAsArray() {
        return this.value.getAsArray();
    }

    /**
     * Get the current element as a JSON
     * primitive
     *
     * @return the element
     */
    @Override
    public @NotNull Optional<JsonPrimitive> getAsPrimitive() {
        return this.value.getAsPrimitive();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an <dfn>{@index "equivalence relation"}</dfn>
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     *
     * <p>
     * An equivalence relation partitions the elements it operates on
     * into <i>equivalence classes</i>; all the members of an
     * equivalence class are equal to each other. Members of an
     * equivalence class are substitutable for each other, at least
     * for some purposes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @implSpec The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * In other words, under the reference equality equivalence
     * relation, each equivalence class only has a single element.
     * @apiNote It is generally necessary to override the {@link #hashCode() hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     * <p>The two-argument {@link Objects#equals(Object,
     * Object) Objects.equals} method implements an equivalence relation
     * on two possibly-null object references.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj instanceof JsonKeyPair kp) {
            return Objects.equals(this.parent, kp.parent) &&
                    Objects.equals(this.key, kp.key) &&
                    Objects.equals(this.value, kp.value);
        }

        if (!(obj instanceof JsonElement element)) return false;
        return Objects.equals(this.key, element.getKey()) &&
                Objects.equals(this.parent, element.getParent().orElse(null)) &&
                Objects.equals(this.value, element);
    }

    /**
     * {@return a hash code value for this object} This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@link
     *     #equals(Object) equals} method, then calling the {@code
     *     hashCode} method on each of the two objects must produce the
     *     same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link #equals(Object) equals} method, then
     *     calling the {@code hashCode} method on each of the two objects
     *     must produce distinct integer results.  However, the programmer
     *     should be aware that producing distinct integer results for
     *     unequal objects may improve the performance of hash tables.
     * </ul>
     *
     * @implSpec As far as is reasonably practical, the {@code hashCode} method defined
     * by class {@code Object} returns distinct integers for distinct objects.
     * @apiNote The {@link Objects#hash(Object...) hash} and {@link
     * Objects#hashCode(Object) hashCode} methods of {@link
     * Objects} can be used to help construct simple hash codes.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.parent, this.key, this.value);
    }
}
