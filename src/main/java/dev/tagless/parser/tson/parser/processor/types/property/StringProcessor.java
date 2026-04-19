package dev.tagless.parser.tson.parser.processor.types.property;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.exception.JsonException;
import dev.tagless.parser.tson.parser.exception.JsonSyntaxException;
import dev.tagless.parser.tson.parser.token.JsonToken;

import java.nio.CharBuffer;

public class StringProcessor extends JsonProcessor {

    private boolean escaped = false;
    private boolean started = false;

    private final StringBuilder builder = new StringBuilder();
    private final boolean isKey;

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     */
    public StringProcessor(final Contextualizer context, final boolean lenient) {
        super(context, lenient);
        this.isKey = false;
    }

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     * @param key if the element is a key
     */
    public StringProcessor(final Contextualizer context, final boolean lenient, final boolean key) {
        super(context, lenient);
        this.isKey = key;
    }

    /**
     * Process the buffer
     *
     * @param buffer the buffer to process
     * @return if the processing has finished
     * @throws JsonException if there's a problem the processor
     *                       can not handle
     */
    @Override
    public boolean process(final CharBuffer buffer) throws JsonException {
        while (buffer.hasRemaining()) {
            this.context.column += 1;
            char character = buffer.get();

            JsonToken token = JsonToken.getToken(character, this.lenient);

            if (this.escaped) {
                this.builder.append(character);
                this.escaped = false;

                continue;
            } else if (character == '\r' || character == '\n' || character == '\f') {
                this.context.lb();
                throw new JsonSyntaxException(this.context, "\\n");
            }

            if (character == '\\') {
                this.escaped = true;
                continue;
            }

            if (token.equals(JsonToken.STRING_BLOCK)) {
                if (started) {
                    JsonToken r = new JsonToken(this.builder.toString());
                    if (this.isKey) {
                        r.asKey();
                    } else {
                        r.asString();
                    }

                    this.result.append(r);
                    this.result.append(token);

                    this.completed = true;
                    return true;
                }

                this.result.append(token);
                this.started = true;
                continue;
            }

            if (this.started) {
                this.builder.append(character);
            }
        }

        return this.completed;
    }
}
