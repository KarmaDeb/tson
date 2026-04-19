package dev.tagless.parser.tson.hydrator.exception;

import dev.tagless.parser.tson.hydrator.Hydrator;
import dev.tagless.parser.tson.parser.token.JsonToken;
import org.jetbrains.annotations.NotNull;

/**
 * The key expected exception is thrown when a production chain
 * finds anything that is not a string where a string should be
 */
public class StringExpectedException extends ProductionChainException {

    /**
     * Initialize the exception
     *
     * @param chain    the current production chain
     * @param found    the token which broke the chain
     */
    public StringExpectedException(final @NotNull Hydrator chain,
                                   final @NotNull JsonToken found) {
        super(chain, String.format("Unexpected token found %s where a string was expected",
                found.getValue()));
    }
}
