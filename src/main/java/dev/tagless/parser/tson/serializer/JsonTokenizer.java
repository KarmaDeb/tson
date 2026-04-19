package dev.tagless.parser.tson.serializer;

import dev.tagless.parser.tson.element.JsonArray;
import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.element.JsonObject;
import dev.tagless.parser.tson.element.JsonPrimitive;
import dev.tagless.parser.tson.parser.token.Token;
import dev.tagless.parser.tson.serializer.tokenizer.type.ArrayTokenizer;
import dev.tagless.parser.tson.serializer.tokenizer.type.ObjectTokenizer;
import dev.tagless.parser.tson.serializer.tokenizer.type.PrimitiveTokenizer;
import org.jetbrains.annotations.NotNull;

/**
 * The JSON tokenizer transforms any {@link JsonElement element} into their
 * {@link Token token} representation
 */
public abstract class JsonTokenizer<T extends JsonElement> {

    @NotNull
    protected final T element;

    /**
     * Creates a new JSON tokenizer
     * @param element the element to tokenize
     */
    public JsonTokenizer(final @NotNull T element) {
        this.element = element;
    }

    /**
     * Process the element
     * @return the processed element
     */
    @NotNull
    public abstract Token process();

    /**
     * Create a new JSON tokenizer
     * @param element the element to tokenize
     * @return the JSON tokenizer
     * @param <T> the element type
     */
    public static <T extends JsonElement> JsonTokenizer<T> create(final T element) {
        return new DefaultTokenizer<>(element);
    }
}

final class DefaultTokenizer<T extends JsonElement> extends JsonTokenizer<T> {

    /**
     * Creates a new JSON tokenizer
     * @param element the element to tokenize
     */
    public DefaultTokenizer(final @NotNull T element) {
        super(element);
    }

    /**
     * Process the element
     * @return the processed element
     */
    @Override
    public @NotNull Token process() {
        JsonTokenizer<? extends JsonElement> target = switch (this.element) {
            case null -> throw new IllegalArgumentException("Cannot tokenize null object");
            case JsonObject o -> new ObjectTokenizer(o);
            case JsonArray a -> new ArrayTokenizer(a);
            case JsonPrimitive p -> new PrimitiveTokenizer(p);
            default -> throw new IllegalStateException("Unexpected value: " + this.element);
        };

        return target.process();
    }
}