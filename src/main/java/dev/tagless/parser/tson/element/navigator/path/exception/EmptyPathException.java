package dev.tagless.parser.tson.element.navigator.path.exception;

import dev.tagless.parser.tson.element.navigator.path.PathProcessor;
import dev.tagless.parser.tson.element.navigator.JsonPath;

/**
 * The empty path exception is thrown when a empty
 * string path is submitted to a {@link PathProcessor} or
 * {@link JsonPath}
 */
public class EmptyPathException extends PathException {

    /**
     * Create a new exception
     */
    public EmptyPathException() {
        super("Can not create a path from an empty string");
    }
}
