package dev.tagless.parser.tson.hydrator.exception;

import dev.tagless.parser.tson.hydrator.Hydrator;
import org.jetbrains.annotations.NotNull;

/**
 * A production chain exception is throw whenever a hydrator
 * cannot correctly process a token chain, thus causing the entire
 * token chain to break
 */
public class ProductionChainException extends Exception {

    /**
     * Initialize the exception
     * @param chain the current production chain
     * @param message the exception message
     */
    public ProductionChainException(final @NotNull Hydrator chain, final @NotNull String message) {
        super(String.format("An exception occurred while processing token chain at %s: %s",
                chain.getClass().getSimpleName(), message));
    }
}
