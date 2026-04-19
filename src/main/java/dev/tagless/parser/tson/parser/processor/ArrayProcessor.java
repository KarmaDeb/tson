package dev.tagless.parser.tson.parser.processor;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.exception.JsonException;
import dev.tagless.parser.tson.parser.exception.JsonSyntaxException;
import dev.tagless.parser.tson.parser.exception.JsonTokenException;
import dev.tagless.parser.tson.parser.processor.lenient.CommentProcessor;
import dev.tagless.parser.tson.parser.processor.types.ArrayItemProcessor;
import dev.tagless.parser.tson.parser.token.JsonToken;

import java.nio.CharBuffer;

public class ArrayProcessor extends JsonProcessor {

    private JsonToken expectedToken = null;
    private boolean start = false;
    private boolean expectsProperty = false;

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     */
    public ArrayProcessor(final Contextualizer context, final boolean lenient) {
        super(context, lenient);
    }

    /**
     * Process the buffer
     *
     * @param buffer the buffer to process
     * @return if the processing has finished
     * @throws JsonException if there's a problem the processor
     * can not handle
     */
    @Override
    public boolean process(final CharBuffer buffer) throws JsonException {
        if (this.checkBuffer(buffer)) {
            return true;
        }
        if (this.delegator != null) {
            if (this.delegator.process(buffer)) {
                if (!(this.delegator instanceof CommentProcessor)) {
                    this.result.extend(this.delegator.getToken());
                    this.expectsProperty = false;
                }

                this.delegator = null;
            } else {
                return false;
            }
        }

        while (buffer.hasRemaining()) {
            this.context.column += 1;

            char character = buffer.get();
            if (Character.isWhitespace(character) || Character.isSpaceChar(character)) {
                if (character == '\n') {
                    this.context.lb();
                }

                continue;
            }

            if (character == '/' || character == '#') {
                if (!lenient) {
                    throw new JsonSyntaxException(this.context, character);
                }

                buffer.position(buffer.position() - 1);
                this.delegator = new CommentProcessor(this.context, true);
                if (this.delegator.process(buffer)) {
                    this.delegator = null;
                    continue;
                }
            }

            JsonToken token = JsonToken.getToken(character, this.lenient);
            if (token.equals(JsonToken.ARRAY_OPEN)) {
                if (!start) {
                    this.result.append(token);

                    start = true;
                    continue;
                }
            } else if (token.equals(JsonToken.ARRAY_CLOSE)) {
                if (!this.start) {
                    throw new JsonTokenException(this.context, character, JsonToken.ARRAY_OPEN);
                }
                if (this.expectsProperty && !lenient) {
                    throw new JsonSyntaxException(this.context, character);
                }

                this.result.append(token);

                this.completed = true;
                return true;
            }

            if (this.expectedToken != null && !token.equals(this.expectedToken)) {
                throw new JsonTokenException(this.context, character, this.expectedToken);
            }

            if (!this.start) {
                throw new JsonTokenException(this.context, character, JsonToken.ARRAY_OPEN);
            }

            if (this.expectedToken != null && token.equals(JsonToken.PROPERTY_SEPARATOR)) {
                this.result.append(token);
                this.expectedToken = null;
                this.expectsProperty = true;

                continue;
            }

            buffer.position(buffer.position() - 1);
            this.delegator = new ArrayItemProcessor(this.context, this.lenient);
            if (this.delegator.process(buffer)) {
                this.result.extend(this.delegator.getToken());
                this.expectedToken = JsonToken.PROPERTY_SEPARATOR;
                this.expectsProperty = false;

                this.delegator = null;
            }
        }

        return this.completed;
    }
}
