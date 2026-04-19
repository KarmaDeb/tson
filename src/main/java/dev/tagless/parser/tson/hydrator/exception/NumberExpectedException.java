package dev.tagless.parser.tson.hydrator.exception;

import dev.tagless.parser.tson.hydrator.Hydrator;
import dev.tagless.parser.tson.parser.token.JsonToken;
import org.jetbrains.annotations.NotNull;

/**
 * The key expected exception is thrown when a production chain
 * finds anything that is not a number where a number should be
 */
public class NumberExpectedException extends ProductionChainException {

    /**
     * Initialize the exception
     *
     * @param chain    the current production chain
     * @param found    the token which broke the chain
     */
    public NumberExpectedException(final @NotNull Hydrator chain,
                                   final @NotNull JsonToken found) {
        super(chain, String.format("Unexpected token found %s where a number was expected",
                found.getValue()));
    }
}
