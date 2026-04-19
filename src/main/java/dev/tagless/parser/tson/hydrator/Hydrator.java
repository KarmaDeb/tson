package dev.tagless.parser.tson.hydrator;

import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.hydrator.exception.ProductionChainException;
import dev.tagless.parser.tson.hydrator.type.ArrayHydrator;
import dev.tagless.parser.tson.hydrator.type.ObjectHydrator;
import dev.tagless.parser.tson.hydrator.type.PrimitiveHydrator;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;
import org.jetbrains.annotations.NotNull;

/**
 * JSON hydrator reads a {@link Token} and returns
 */
public abstract class Hydrator {

    protected final HydratorChain chain;

    /**
     * Create a new hydrator
     * @param chain the hydrator chain
     */
    public Hydrator(final @NotNull HydratorChain chain) {
        this.chain = chain;
    }

    /**
     * Process the token chain
     * @return the processed element
     * @throws ProductionChainException if the production chain
     * breaks in the process
     */
    @NotNull
    public abstract JsonElement process() throws ProductionChainException;

    /**
     * Creates a new hydrator
     * @param token the token to process
     * @return the hydrator
     */
    public static Hydrator create(final @NotNull Token token) {
        return new DefaultHydrator(token);
    }
}

class DefaultHydrator extends Hydrator {

    /**
     * Create a new hydrator
     *
     * @param token the hydrator token
     */
    public DefaultHydrator(final @NotNull Token token) {
        super(new HydratorChain(token));
    }

    /**
     * Process the token chain
     * @return the processed element
     * @throws ProductionChainException if the production chain
     * breaks in the process
     */
    @Override
    public @NotNull JsonElement process() throws ProductionChainException {
        if (!this.chain.hasNext()) {
            return new JsonPrimitive();
        }

        JsonToken token = this.chain.next();
        this.chain.previous();

        if (token.equals(JsonToken.BLOCK_OPEN)) {
            return new ObjectHydrator(this.chain).process();
        } else if (token.equals(JsonToken.ARRAY_OPEN)) {
            return new ArrayHydrator(this.chain).process();
        }

        return new PrimitiveHydrator(this.chain).process();
    }
}