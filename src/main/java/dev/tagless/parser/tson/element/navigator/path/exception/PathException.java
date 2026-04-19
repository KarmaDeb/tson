package dev.tagless.parser.tson.element.navigator.path.exception;

import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a path problem
 * occurs. Usually one related to path processing
 */
public class PathException extends Exception {

    /**
     * Create the exception
     * @param message the exception message
     */
    public PathException(final @NotNull String message) {
        super(message);
    }
}
