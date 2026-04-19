package dev.tagless.parser.tson.parser.exception;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.token.JsonToken;
import org.jetbrains.annotations.NotNull;

/**
 * The token exception is thrown when an unexpected token
 * is found while processing a JSON document
 */
public class JsonTokenException extends JsonException {

    /**
     * Initialize the exception
     * @param context the current parser context
     * @param found the current character
     * @param expected the expecting characters
     */
    public JsonTokenException(final @NotNull JsonProcessor.Contextualizer context,
                              final char found, final @NotNull JsonToken expected) {
        super(context, String.format("Bad token while processing json \"%s\" (where %s was expected)",
                found,
                expected.getValue()
        ));
    }
}
