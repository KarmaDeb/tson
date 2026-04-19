package dev.tagless.parser.tson.element.navigator.path.exception;

import dev.tagless.parser.tson.element.navigator.path.PathToken;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This exception is thrown when a token is found where
 * a sub-set of tokens was expected
 */
public class BadTokenPath extends PathException {

    /**
     * Create a new exception
     * @param found the found token
     * @param expected the expected tokens
     */
    public BadTokenPath(final PathToken.Type found, final PathToken.Type... expected) {
        super(String.format("Bad token found while processing path: %s. Was expected any of: %s",
                found,
                Arrays.stream(expected).map(PathToken.Type::name).collect(Collectors.joining(", "))));
    }
}
