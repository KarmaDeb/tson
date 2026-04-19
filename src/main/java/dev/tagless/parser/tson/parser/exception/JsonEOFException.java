package dev.tagless.parser.tson.parser.exception;

import dev.tagless.parser.tson.parser.JsonProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * The EOF exception is thrown when a token is detected
 * where an EOF was expected
 */
public class JsonEOFException extends JsonException {

    /**
     * Initialize the exception
     * @param context the current parser context
     * @param found the current character
     */
    public JsonEOFException(final @NotNull JsonProcessor.Contextualizer context, final char found) {
        super(context, String.format("Bad token while processing json \"%s\" (where EOF was expected)", found));
    }
}
