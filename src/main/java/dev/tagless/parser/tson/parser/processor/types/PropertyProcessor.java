package dev.tagless.parser.tson.parser.processor.types;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.exception.JsonException;
import dev.tagless.parser.tson.parser.exception.JsonSyntaxException;
import dev.tagless.parser.tson.parser.exception.JsonTokenException;
import dev.tagless.parser.tson.parser.processor.ArrayProcessor;
import dev.tagless.parser.tson.parser.processor.ObjectProcessor;
import dev.tagless.parser.tson.parser.processor.lenient.CommentProcessor;
import dev.tagless.parser.tson.parser.processor.types.property.AnyProcessor;
import dev.tagless.parser.tson.parser.processor.types.property.StringProcessor;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;

import java.nio.CharBuffer;

public class PropertyProcessor extends JsonProcessor {

    private JsonToken expectedToken = JsonToken.STRING_BLOCK;
    private Token key;

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     */
    public PropertyProcessor(final Contextualizer context, final boolean lenient) {
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
                    if (this.key == null) {
                        this.key = this.delegator.getToken();
                        this.expectedToken = JsonToken.VALUE_INDICATOR;
                    }

                    this.result.extend(this.delegator.getToken());

                    this.completed = true;
                    return true;
                }

                this.delegator = null;
            }

            return false;
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
            if (expectedToken != null && !token.equals(this.expectedToken)) {
                if (this.lenient && expectedToken.equals(JsonToken.VALUE_INDICATOR)) {
                    expectedToken = null;
                    this.result.append(JsonToken.VALUE_INDICATOR);
                    buffer.position(buffer.position() - 1);

                    continue;
                }

                if (this.lenient && (token.equals(JsonToken.BLOCK_CLOSE) || token.equals(JsonToken.ARRAY_CLOSE))) {
                    buffer.position(buffer.position() - 1);

                    this.result = new Token(JsonToken.UNKNOWN);
                    this.completed = true;
                    return true;
                }

                throw new JsonTokenException(this.context, character, this.expectedToken);
            }

            if (token.equals(JsonToken.VALUE_INDICATOR)) {
                this.expectedToken = null;
                this.result.append(token);
                continue;
            }

            if (token.equals(JsonToken.BLOCK_OPEN) || token.equals(JsonToken.ARRAY_OPEN)) {
                buffer.position(buffer.position() - 1);
                this.delegator = token.equals(JsonToken.BLOCK_OPEN) ?
                        new ObjectProcessor(this.context, this.lenient) :
                        new ArrayProcessor(this.context, this.lenient);

                if (this.delegator.process(buffer)) {
                    this.result.extend(this.delegator.getToken());
                    this.completed = true;

                    return true;
                }
            } else if (token.equals(JsonToken.STRING_BLOCK)) {
                buffer.position(buffer.position() - 1);
                this.delegator = new StringProcessor(this.context, this.lenient, this.key == null);
                if (this.delegator.process(buffer)) {
                    if (this.key == null) {
                        this.key = this.delegator.getToken();
                        this.expectedToken = JsonToken.VALUE_INDICATOR;

                        this.result.extend(this.key);
                        continue;
                    }

                    this.result.extend(this.delegator.getToken());

                    this.completed = true;
                    return true;
                }
            } else {
                buffer.position(buffer.position() - 1);
                this.delegator = new AnyProcessor(this.context, this.lenient);
                if (this.delegator.process(buffer)) {
                    this.result.extend(this.delegator.getToken());
                    this.completed = true;

                    return true;
                }
            }
        }

        return this.completed;
    }
}
