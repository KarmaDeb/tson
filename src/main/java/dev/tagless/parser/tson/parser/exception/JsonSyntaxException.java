package dev.tagless.parser.tson.parser.exception;

import dev.tagless.parser.tson.parser.JsonProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * The syntax exception is thrown when an unexpected token
 * is found while processing a JSON document
 */
public class JsonSyntaxException extends JsonException {

    /**
     * Initialize the exception
     * @param context the current parser context
     * @param found the current character
     */
    public JsonSyntaxException(final @NotNull JsonProcessor.Contextualizer context,
                               final char found) {
        super(context, String.format("Unexpected character while processing json \"%s\"", found));
    }

    /**
     * Initialize the exception
     * @param context the current parser context
     * @param name the current character name
     */
    public JsonSyntaxException(final @NotNull JsonProcessor.Contextualizer context,
                               final String name) {
        super(context, String.format("Unexpected character while processing json \"%s\"", name));
    }
}
