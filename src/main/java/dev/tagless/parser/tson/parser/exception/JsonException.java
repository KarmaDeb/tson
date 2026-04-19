package dev.tagless.parser.tson.parser.exception;

import dev.tagless.parser.tson.parser.JsonProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * Generic JSON exception
 */
public class JsonException extends Exception {

    /**
     * Initialize the exception
     * @param context the document context
     * @param message the exception message
     */
    public JsonException(final @NotNull JsonProcessor.Contextualizer context, final @NotNull String message) {
        super(String.format("Error while processing json document at %d:%d: %s",
                context.line, context.column, message));
    }
}
