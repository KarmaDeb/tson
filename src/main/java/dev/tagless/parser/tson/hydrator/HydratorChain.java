package dev.tagless.parser.tson.hydrator;

import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The hydrator chain takes a token and splits
 * each into a one-way chain
 */
public class HydratorChain implements Iterator<JsonToken> {

    private Token currentToken;

    public HydratorChain(final @NotNull Token token) {
        this.currentToken = token.first();
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return this.currentToken != null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public @NotNull JsonToken next() {
        if (this.currentToken == null) {
            throw new NoSuchElementException();
        }

        JsonToken jt = this.currentToken.get();
        this.currentToken = this.currentToken.next();

        return jt;
    }

    /**
     * Returns the previous element in the iteration.
     * @return the previous element in the iteration
     */
    public @NotNull JsonToken previous() {
        if (this.currentToken.previous() == null) {
            return this.currentToken.get();
        }

        JsonToken jt = this.currentToken.get();
        this.currentToken = this.currentToken.previous();

        return jt;
    }
}
