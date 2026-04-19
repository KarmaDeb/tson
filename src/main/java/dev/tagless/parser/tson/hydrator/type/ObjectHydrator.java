package dev.tagless.parser.tson.hydrator.type;

import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.helper.JsonKeyPair;
import dev.tagless.parser.tson.hydrator.Hydrator;
import dev.tagless.parser.tson.hydrator.HydratorChain;
import dev.tagless.parser.tson.hydrator.exception.BadTokenException;
import dev.tagless.parser.tson.hydrator.exception.ProductionChainException;
import dev.tagless.parser.tson.hydrator.type.helper.KeyPairHydrator;
import dev.tagless.parser.tson.parser.token.JsonToken;
import org.jetbrains.annotations.NotNull;

/**
 * Hydrator for object JSON types
 */
public final class ObjectHydrator extends Hydrator {

    private JsonToken expectedToken = JsonToken.BLOCK_OPEN;
    private boolean start = false;
    private boolean canMatureClose = true;

    /**
     * Create a new hydrator
     *
     * @param chain the hydrator chain
     */
    public ObjectHydrator(final @NotNull HydratorChain chain) {
        super(chain);
    }

    /**
     * Process the token chain
     * @return the processed element
     * @throws ProductionChainException if the production chain
     * breaks in the process
     */
    @Override
    public @NotNull JsonObject process() throws ProductionChainException {
        JsonObject object = new JsonObject();

        while (this.chain.hasNext()) {
            JsonToken token = this.chain.next();
            if (token.equals(JsonToken.BLOCK_CLOSE)) {
                if (!start || (!this.canMatureClose && this.expectedToken.equals(JsonToken.STRING_BLOCK))) {
                    throw new BadTokenException(this, token, this.expectedToken);
                }

                break;
            }

            if (!token.equals(this.expectedToken)) {
                throw new BadTokenException(this, token, this.expectedToken);
            }

            this.canMatureClose = false;
            if (token.equals(JsonToken.BLOCK_OPEN)) {
                this.start = true;
                this.canMatureClose = true;
                this.expectedToken = JsonToken.STRING_BLOCK;
                continue;
            }

            if (token.equals(JsonToken.PROPERTY_SEPARATOR)) {
                this.expectedToken = JsonToken.STRING_BLOCK;
                continue;
            }

            if (token.equals(JsonToken.STRING_BLOCK)) {
                this.chain.previous();

                KeyPairHydrator hydrator = new KeyPairHydrator(object, this.chain);
                JsonKeyPair keyPair = hydrator.process();

                object.add(keyPair);
                this.expectedToken = JsonToken.PROPERTY_SEPARATOR;
            }
        }

        return object;
    }
}
