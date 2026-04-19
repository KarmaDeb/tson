package dev.tagless.parser.tson.parser.token;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * A token represents a set of {@link JsonToken}
 */
@SuppressWarnings("unused")
public class Token {

    private final SharedRef tail;
    private final SharedRef head;

    private Token next;
    private Token previous;

    private final JsonToken token;

    /**
     * Create a new token
     * @param token the token
     */
    public Token(final JsonToken token) {
        this.token = token;

        this.tail = new SharedRef();
        this.head = new SharedRef();

        this.tail.value = this;
        this.head.value = this;
    }

    /**
     * Create a new token
     * @param token the token
     */
    private Token(final JsonToken token, final SharedRef tail, final SharedRef head) {
        this.token = token;

        this.tail = tail;
        this.head = head;
    }

    /**
     * Get the token
     * @return the token
     */
    @NotNull
    public JsonToken get() {
        return this.token;
    }

    /**
     * Set the last token
     * @param token the last token
     */
    public void append(final JsonToken token) {
        if (JsonToken.UNKNOWN.equals(token)) {
            return;
        }

        Token t = new Token(token, this.tail, this.head);
        Token head = this.head.value;

        this.head.value = t;

        head.next = t;
        t.previous = head;
    }

    /**
     * Set the first token
     * @param token the first token
     */
    public void prepend(final JsonToken token) {
        if (JsonToken.UNKNOWN.equals(token)) {
            return;
        }

        Token t = new Token(token, this.tail, this.head);
        Token tail = this.tail.value;

        this.tail.value = t;

        tail.previous = t;
        t.next = tail;
    }

    /**
     * Get the next token
     * @return the next token
     */
    public Token next() {
        return this.next;
    }

    /**
     * Get the previous token
     * @return the previous token
     */
    public Token previous() {
        return this.previous;
    }

    /**
     * Extend this token with another
     * token
     * @param other the token to extend with
     */
    public void extend(Token other) {
        while (other != null) {
            this.head.value.append(other.token);
            other = other.next;
        }
    }

    /**
     * Get the last token
     * @return the last token
     */
    @NotNull
    public Token last() {
        return this.head.value;
    }

    /**
     * Get the first token
     * @return the first token
     */
    @NotNull
    public Token first() {
        return this.tail.value;
    }

    public void remove() {
        Token pre = this.previous;
        Token nex = this.next;

        if (pre != null) {
            pre.next = nex;
        }
        if (nex != null) {
            nex.previous = pre;
        }

        if (this.head.value == this) {
            this.head.value = pre;
        }
        if (this.tail.value == this) {
            this.tail.value = nex;
        }
    }

    public void clean() {
        Token n = this.head.value;
        while (n != null) {
            if (JsonToken.UNKNOWN.equals(n.token)) {
                n.remove();
            }
            n = n.next;
        }
    }

    /**
     * Get the next token
     * @return the next token
     */
    @NotNull
    public Optional<JsonToken> nextToken() {
        return Optional.ofNullable(this.next).map(t -> t.token);
    }

    /**
     * Get the previous token
     * @return the previous token
     */
    @NotNull
    public Optional<JsonToken> previousToken() {
        return Optional.ofNullable(this.previous).map(t -> t.token);
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
        Token firstToken = this.tail.value;

        if (this != firstToken) {
            return firstToken.toString();
        }

        StringBuilder builder = new StringBuilder();
        Token current = this;

        builder.append(current.token);

        while (current.next != null) {
            current = current.next;
            builder.append(" -> ").append(current.token);
        }

        return builder.toString();
    }
}

class SharedRef {
    Token value;
}