package dev.tagless.parser.tson.hydrator.type;

import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.hydrator.Hydrator;
import dev.tagless.parser.tson.hydrator.HydratorChain;
import dev.tagless.parser.tson.hydrator.exception.*;
import dev.tagless.parser.tson.parser.token.JsonToken;
import org.jetbrains.annotations.NotNull;

/**
 * Hydrator for primitive JSON types
 */
public final class PrimitiveHydrator extends Hydrator {

    /**
     * Create a new hydrator
     *
     * @param chain the hydrator chain
     */
    public PrimitiveHydrator(final @NotNull HydratorChain chain) {
        super(chain);
    }

    /**
     * Process the token chain
     * @return the processed element
     * @throws ProductionChainException if the production chain
     * breaks in the process
     *
     */
    @Override
    public @NotNull JsonPrimitive process() throws ProductionChainException {
        if (!chain.hasNext()) {
            throw new ProductionChainException(this, "Chain overflow while processing json primitive");
        }

        JsonToken token = chain.next();
        if (token.isValue()) {
            String raw = token.getValue();
            if (token.isBoolean()) {
                if (!raw.equals("true") && !raw.equals("false")) {
                    throw new BooleanExpectedException(this, token);
                }

                return new JsonPrimitive(raw.equals("true"));
            } else if (token.isNull()) {
                if (!raw.equals("null")) {
                    throw new NullExpectedException(this, token);
                }

                return new JsonPrimitive();
            } else if (token.isNumber()) {
                try {
                    Number number = JsonPrimitive.number(raw);
                    return new JsonPrimitive(number);
                } catch (NumberFormatException ex) {
                    throw new NumberExpectedException(this, token);
                }
            } else if (token.isString()) {
                throw new BadTokenException(this, token, JsonToken.STRING_BLOCK);
            } else {
                throw new ValueExpectedException(this, token);
            }
        } else if (!token.equals(JsonToken.STRING_BLOCK)) {
            throw new BadTokenException(this, token, JsonToken.STRING_BLOCK);
        }

        token = chain.next();
        if (!token.isValue()) {
            throw new ValueExpectedException(this, token);
        }
        if (!token.isString()) {
            throw new StringExpectedException(this, token);
        }

        String value = token.getValue();
        token = chain.next();
        if (!token.equals(JsonToken.STRING_BLOCK)) {
            throw new BadTokenException(this, token, JsonToken.STRING_BLOCK);
        }

        return new JsonPrimitive(value);
    }
}
