package dev.tagless.parser.tson.element.navigator.path.exception;

import dev.tagless.parser.tson.element.navigator.path.PathToken;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This exception is thrown when a token is found where
 * a sub-set of tokens was expected
 */
public class BadTokenPosition extends PathException {

    /**
     * Create a new exception
     * @param position the start position
     * @param found the found token
     * @param expected the expected tokens
     */
    public BadTokenPosition(final int position,
                            final char found,
                            final PathToken.Type... expected) {
        super(String.format("Bad token found while processing path at index %d: %s. Was expected any of: %s",
                position,
                found,
                Arrays.stream(expected).map(PathToken.Type::name).collect(Collectors.joining(", "))));
    }
}
