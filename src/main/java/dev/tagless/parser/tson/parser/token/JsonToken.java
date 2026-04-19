package dev.tagless.parser.tson.parser.token;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

/**
 * A token represents a specific keyword in a
 * JSON document. For instance "{" represents a
 * new block start, while "}" represents a block
 * end
 */
@SuppressWarnings("unused")
public final class JsonToken {

    private static final byte KEY = 1 << 1;
    private static final byte VALUE = 1 << 2;
    private static final byte NULL = 1 << 3;
    private static final byte BOOLEAN = 1 << 4;
    private static final byte NUMBER = 1 << 5;
    private static final byte STRING = 1 << 6;

    public static final JsonToken VALUE_INDICATOR = new JsonToken(':');
    public static final JsonToken PROPERTY_SEPARATOR = new JsonToken(',');

    public static final JsonToken STRING_BLOCK = new JsonToken('"', new JsonToken('\''));
    public static final JsonToken BLOCK_OPEN = new JsonToken('{');
    public static final JsonToken BLOCK_CLOSE = new JsonToken('}');
    public static final JsonToken ARRAY_OPEN = new JsonToken('[');
    public static final JsonToken ARRAY_CLOSE = new JsonToken(']');
    public static final JsonToken KEYWORD_TRUE = new JsonToken("true").asBoolean();
    public static final JsonToken KEYWORD_FALSE = new JsonToken("false").asBoolean();
    public static final JsonToken KEYWORD_NULL = new JsonToken("null").asNull();

    static {
        KEYWORD_TRUE.metadata = (VALUE | BOOLEAN);
        KEYWORD_FALSE.metadata = (VALUE | BOOLEAN);
        KEYWORD_NULL.metadata = (VALUE | NULL);
    }

    public static final JsonToken UNKNOWN = new JsonToken('\0');

    public static JsonToken[] values() {
        return new JsonToken[]{
                VALUE_INDICATOR,
                STRING_BLOCK,
                BLOCK_OPEN,
                BLOCK_CLOSE,
                ARRAY_OPEN,
                ARRAY_CLOSE,
                PROPERTY_SEPARATOR,
                KEYWORD_TRUE,
                KEYWORD_FALSE
        };
    }

    private final Character character;
    private final JsonToken lenientCharacter;
    private final String raw;

    private int metadata = 0;

    private JsonToken(final Character character) {
        this(character, null);
    }

    private JsonToken(final Character character, final JsonToken lenientCharacter) {
        this.character = character;
        this.lenientCharacter = lenientCharacter;
        this.raw = character == null ? "" : String.valueOf(character);
    }

    /**
     * Creates a new JSON token
     * @param value the token value
     */
    public JsonToken(final String value) {
        this.character = null;
        this.lenientCharacter = null;
        this.raw = value;
    }

    /**
     * Get if the token is a key
     * @return the token meta flag
     */
    public boolean isKey() {
        return (this.metadata & KEY) == KEY;
    }

    /**
     * Get if the token is a value
     * @return the token meta flag
     */
    public boolean isValue() {
        return (this.metadata & VALUE) == VALUE;
    }

    /**
     * Get if the token is null
     * @return the token meta flag
     */
    public boolean isNull() {
        return (this.metadata & NULL) == NULL;
    }

    /**
     * Get if the token is a boolean
     * @return the token meta flag
     */
    public boolean isBoolean() {
        return (this.metadata & BOOLEAN) == BOOLEAN;
    }

    /**
     * Get if the token is a number
     * @return the token meta flag
     */
    public boolean isNumber() {
        return (this.metadata & NUMBER) == NUMBER;
    }

    /**
     * Get if the token is a string
     * @return the token meta flag
     */
    public boolean isString() {
        return (this.metadata & STRING) == STRING;
    }

    /**
     * Get the token value
     * @return the value of the token
     */
    @NotNull
    public String getValue() {
        return this.raw;
    }

    /**
     * Enable the string flag for the
     * token
     * @return the token
     */
    public JsonToken asKey() {
        if (this.character != null) {
            throw new IllegalStateException("Cannot change metadata of predefined token: " + this.character);
        }

        this.metadata = (KEY | STRING);
        return this;
    }

    /**
     * Enable the string flag for the
     * token
     * @return the token
     */
    public JsonToken asValue() {
        if (this.character != null) {
            throw new IllegalStateException("Cannot change metadata of predefined token: " + this.character);
        }

        this.metadata = (VALUE | STRING);
        return this;
    }

    /**
     * Enable the string flag for the
     * token
     * @return the token
     */
    public JsonToken asNull() {
        if (this.character != null) {
            throw new IllegalStateException("Cannot change metadata of predefined token: " + this.character);
        }

        if (!this.raw.equals("null")) {
            throw new IllegalStateException("Cannot mark a not null json token as null");
        }

        this.metadata = (VALUE | NULL);
        return this;
    }

    /**
     * Enable the string flag for the
     * token
     * @return the token
     */
    public JsonToken asBoolean() {
        if (this.character != null) {
            throw new IllegalStateException("Cannot change metadata of predefined token: " + this.character);
        }

        if (!this.raw.equals("true") && !this.raw.equals("false")) {
            throw new IllegalStateException("Cannot mark a not boolean json token as boolean");
        }

        this.metadata = (VALUE | BOOLEAN);
        return this;
    }

    /**
     * Enable the string flag for the
     * token
     * @return the token
     */
    public JsonToken asNumber() {
        if (this.character != null) {
            throw new IllegalStateException("Cannot change metadata of predefined token: " + this.character);
        }

        if (!this.raw.matches("^[+-]?((\\d+\\.?\\d*)|(\\.\\d+))([eE][+-]?\\d+)?$")) {
            throw new IllegalStateException("Cannot mark a not numeric json token as numeric");
        }

        this.metadata = (VALUE | NUMBER);
        return this;
    }

    /**
     * Enable the string flag for the
     * token
     * @return the token
     */
    public JsonToken asString() {
        if (this.character != null) {
            throw new IllegalStateException("Cannot change metadata of predefined token: " + this.character);
        }

        this.metadata = (VALUE | STRING);
        return this;
    }

    /**
     * Get the token as a lenient token
     * @return the lenient token
     */
    @NotNull
    public JsonToken asLenient() {
        return Objects.requireNonNullElse(this.lenientCharacter, this);
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
        String value = this.raw;
        if (this.isKey()) {
            return String.format("(KEY) %s", value);
        } else if (this.isNull()) {
            return String.format("(NULL) %s", value);
        } else if (this.isBoolean()) {
            return String.format("(BOOL) %s", value);
        } else if (this.isNumber()) {
            return String.format("(NUMBER) %s", value);
        } else if (this.isString()) {
            return String.format("(STRING) %s", value);
        } else if (this.isValue()) {
            return String.format("(VALUE) %s", value);
        }

        return value;
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
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof JsonToken token)) {
            return false;
        }

        if (!token.raw.isBlank()) {
            // For instance, a custom created token == UNKNOWN, but UNKNOWN != a custom created token
            return this.character == token.character &&
                Objects.equals(this.lenientCharacter, token.lenientCharacter) &&
                    token.raw.equals(this.raw);
        }

        return this.character == token.character &&
                Objects.equals(this.lenientCharacter, token.lenientCharacter);
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
        return Objects.hash(this.character, this.lenientCharacter);
    }

    /**
     * Find a token from the specified character
     * @param character the character being parsed
     * @return the matching token
     */
    @NotNull
    public static JsonToken getToken(final char character) {
        return getToken(character, false);
    }

    /**
     * Find a token from the specified character
     * @param character the character being parsed
     * @param lenient allow lenient parser
     * @return the matching token
     */
    @NotNull
    public static JsonToken getToken(final char character, final boolean lenient) {
        for (JsonToken token : JsonToken.values()) {
            if (token.character == null) {
                continue;
            }

            if (token.character == character) {
                return token;
            }

            if (lenient) {
                JsonToken lenientToken = token.asLenient();
                if (lenientToken.equals(token)) {
                    continue;
                }

                if (lenientToken.character == character) {
                    return token;
                }
            }
        }

        return UNKNOWN;
    }
}
