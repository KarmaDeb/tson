package dev.tagless.parser.tson.serializer.tokenizer.type;

import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;
import dev.tagless.parser.tson.serializer.JsonTokenizer;
import org.jetbrains.annotations.NotNull;

/**
 * JsonPrimitive tokenizer
 */
public class PrimitiveTokenizer extends JsonTokenizer<JsonPrimitive> {

    /**
     * Creates a new JSON tokenizer
     *
     * @param element the element to tokenize
     */
    public PrimitiveTokenizer(final @NotNull JsonPrimitive element) {
        super(element);
    }

    /**
     * Process the element
     *
     * @return the processed element
     */
    @Override
    public @NotNull Token process() {
        if (this.element.isString()) {
            Token rootToken = new Token(JsonToken.STRING_BLOCK);
            rootToken.append(new JsonToken(this.element.getString()).asString());
            rootToken.append(JsonToken.STRING_BLOCK);

            return rootToken.first();
        } else if (this.element.isNumber()) {
            return new Token(new JsonToken(
                    this.element.getAsString()
            ).asNumber());
        } else if (this.element.isBoolean()) {
            if (this.element.getBoolean()) {
                return new Token(JsonToken.KEYWORD_TRUE);
            }

            return new Token(JsonToken.KEYWORD_FALSE);
        } else {
            return new Token(JsonToken.KEYWORD_NULL);
        }
    }
}
