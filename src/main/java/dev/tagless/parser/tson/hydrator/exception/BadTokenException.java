package dev.tagless.parser.tson.hydrator.exception;

import dev.tagless.parser.tson.hydrator.Hydrator;
import dev.tagless.parser.tson.parser.token.JsonToken;
import org.jetbrains.annotations.NotNull;

/**
 * The bad token exception is thrown when a production chain
 * finds a token where it should not be
 */
public class BadTokenException extends ProductionChainException {

    /**
     * Initialize the exception
     *
     * @param chain    the current production chain
     * @param found    the token which broke the chain
     * @param expected the expected token in the chain
     */
    public BadTokenException(final @NotNull Hydrator chain, final @NotNull JsonToken found,
                             final @NotNull JsonToken expected) {
        super(chain, String.format("Unexpected token found %s where \"%s\" was expected",
                found.getValue(),
                expected.getValue()));
    }
}
