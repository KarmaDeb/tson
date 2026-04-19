package dev.tagless.parser.tson.hydrator.type;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.hydrator.Hydrator;
import dev.tagless.parser.tson.hydrator.HydratorChain;
import dev.tagless.parser.tson.hydrator.exception.BadTokenException;
import dev.tagless.parser.tson.hydrator.exception.ProductionChainException;
import dev.tagless.parser.tson.parser.token.JsonToken;
import org.jetbrains.annotations.NotNull;

/**
 * Hydrator for array JSON types
 */
public final class ArrayHydrator extends Hydrator {

    private JsonToken expectedToken = JsonToken.ARRAY_OPEN;
    private boolean start = false;
    private boolean canMatureClose = true;

    /**
     * Create a new hydrator
     *
     * @param chain the hydrator chain
     */
    public ArrayHydrator(final @NotNull HydratorChain chain) {
        super(chain);
    }

    /**
     * Process the token chain
     * @return the processed element
     * @throws ProductionChainException if the production chain
     * breaks in the process
     */
    @Override
    public @NotNull JsonArray process() throws ProductionChainException {
        JsonArray array = new JsonArray();

        while (this.chain.hasNext()) {
            JsonToken token = this.chain.next();

            if (token.equals(JsonToken.ARRAY_CLOSE)) {
                if (this.expectedToken == JsonToken.PROPERTY_SEPARATOR) {
                    this.expectedToken = null;
                }

                if (this.expectedToken != null && (!start || !this.canMatureClose)) {
                    throw new BadTokenException(this, token, this.expectedToken);
                }

                break;
            }

            if (this.expectedToken != null && !token.equals(this.expectedToken)) {
                throw new BadTokenException(this, token, this.expectedToken);
            }

            if (this.expectedToken != null && token.equals(JsonToken.PROPERTY_SEPARATOR)) {
                this.expectedToken = null;
                continue;
            }

            this.canMatureClose = false;
            if (token.equals(JsonToken.ARRAY_OPEN) && !this.start) {
                this.start = true;
                this.canMatureClose = true;
                this.expectedToken = null;
                continue;
            }

            this.chain.previous();
            if (token.equals(JsonToken.BLOCK_OPEN) || token.equals(JsonToken.ARRAY_OPEN)) {
                Hydrator hydrator = token.equals(JsonToken.BLOCK_OPEN) ?
                        new ObjectHydrator(this.chain) :
                        new ArrayHydrator(this.chain);

                JsonElement processed = hydrator.process();
                array.add(processed);
                this.expectedToken = JsonToken.PROPERTY_SEPARATOR;
                continue;
            }

            PrimitiveHydrator hydrator = new PrimitiveHydrator(this.chain);
            JsonPrimitive primitive = hydrator.process();
            array.add(primitive);
            this.expectedToken = JsonToken.PROPERTY_SEPARATOR;
        }

        return array;
    }
}
