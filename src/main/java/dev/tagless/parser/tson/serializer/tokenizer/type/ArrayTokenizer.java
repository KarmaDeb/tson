package dev.tagless.parser.tson.serializer.tokenizer.type;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;
import dev.tagless.parser.tson.serializer.JsonTokenizer;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * JsonArray tokenizer
 */
public class ArrayTokenizer extends JsonTokenizer<JsonArray> {

    /**
     * Creates a new JSON tokenizer
     *
     * @param element the element to tokenize
     */
    public ArrayTokenizer(final @NotNull JsonArray element) {
        super(element);
    }

    /**
     * Process the element
     *
     * @return the processed element
     */
    @Override
    public @NotNull Token process() {
        Token rootToken = new Token(JsonToken.ARRAY_OPEN);

        Iterator<JsonElement> iterator = this.element.iterator();
        while (iterator.hasNext()) {
            JsonElement item = iterator.next();
            JsonTokenizer<?> tokenizer = switch (item) {
                case null -> throw new IllegalArgumentException("Cannot tokenize null object");
                case JsonObject o -> new ObjectTokenizer(o);
                case JsonArray a -> new ArrayTokenizer(a);
                case JsonPrimitive p -> new PrimitiveTokenizer(p);
                default -> throw new IllegalStateException("Unexpected value: " + this.element);
            };

            Token token = tokenizer.process().first();
            while (token != null) {
                rootToken.append(token.get());
                token = token.next();
            }

            if (iterator.hasNext()) {
                rootToken.append(JsonToken.PROPERTY_SEPARATOR);
            }
        }
        rootToken.append(JsonToken.ARRAY_CLOSE);

        return rootToken.first();
    }
}
