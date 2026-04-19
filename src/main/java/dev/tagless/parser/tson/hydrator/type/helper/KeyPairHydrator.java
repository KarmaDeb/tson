package dev.tagless.parser.tson.hydrator.type.helper;

import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.element.helper.JsonKeyPair;
import dev.tagless.parser.tson.hydrator.Hydrator;
import dev.tagless.parser.tson.hydrator.HydratorChain;
import dev.tagless.parser.tson.hydrator.exception.BadTokenException;
import dev.tagless.parser.tson.hydrator.exception.KeyExpectedException;
import dev.tagless.parser.tson.hydrator.exception.ProductionChainException;
import dev.tagless.parser.tson.hydrator.exception.StringExpectedException;
import dev.tagless.parser.tson.hydrator.type.ArrayHydrator;
import dev.tagless.parser.tson.hydrator.type.ObjectHydrator;
import dev.tagless.parser.tson.hydrator.type.PrimitiveHydrator;
import dev.tagless.parser.tson.parser.token.JsonToken;
import org.jetbrains.annotations.NotNull;

/**
 * The key pair hydrator allows to easily parse key:pair
 * chains in a token chain
 */
public class KeyPairHydrator extends Hydrator {

    private final JsonObject parent;

    private JsonToken expectedToken = JsonToken.STRING_BLOCK;
    private String activeKey = null;
    private boolean expectsValue = false;
    private boolean expectsString = false;

    /**
     * Create a new hydrator
     *
     * @param parent the key:pair parent
     * @param chain the hydrator chain
     */
    public KeyPairHydrator(final @NotNull JsonObject parent, final @NotNull HydratorChain chain) {
        super(chain);
        this.parent = parent;
    }

    /**
     * Process the token chain
     *
     * @return the processed element
     * @throws ProductionChainException if the production chain
     *                                  breaks in the process
     */
    @Override
    public @NotNull JsonKeyPair process() throws ProductionChainException {
        while (this.chain.hasNext()) {
            JsonToken token = this.chain.next();

            if (!expectsValue && !token.equals(this.expectedToken)) {
                if (token.isKey() && this.activeKey == null) {
                    if (!token.isString()) {
                        throw new StringExpectedException(this, token);
                    }

                    this.activeKey = token.getValue();
                    continue;
                }
                if (this.activeKey == null && this.expectedToken.equals(JsonToken.STRING_BLOCK)) {
                    throw new KeyExpectedException(this, token);
                }

                throw new BadTokenException(this, token, this.expectedToken);
            }

            if (token.equals(JsonToken.STRING_BLOCK)) {
                if (this.activeKey == null || this.expectsValue) {
                    if (this.expectsValue) {
                        this.expectsString = true;
                    }
                    continue;
                }

                this.expectedToken = JsonToken.VALUE_INDICATOR;
                continue;
            }

            if (token.equals(JsonToken.VALUE_INDICATOR)) {
                this.expectsValue = true;
                continue;
            }

            this.chain.previous();
            if (token.equals(JsonToken.BLOCK_OPEN) || token.equals(JsonToken.ARRAY_OPEN)) {
                Hydrator hydrator = token.equals(JsonToken.BLOCK_OPEN) ?
                        new ObjectHydrator(this.chain) :
                        new ArrayHydrator(this.chain);

                JsonElement processed = hydrator.process();
                return new JsonKeyPair(this.parent, this.activeKey, processed);
            }

            if (this.expectsString) {
                chain.previous();
            }
            PrimitiveHydrator hydrator = new PrimitiveHydrator(this.chain);
            JsonPrimitive primitive = hydrator.process();

            return new JsonKeyPair(this.parent, this.activeKey, primitive);
        }

        throw new ProductionChainException(this, "Could not determine current key:value pair");
    }
}
