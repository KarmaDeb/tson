package dev.tagless.parser.tson.parser.processor;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.exception.JsonException;
import dev.tagless.parser.tson.parser.exception.JsonSyntaxException;
import dev.tagless.parser.tson.parser.exception.JsonTokenException;
import dev.tagless.parser.tson.parser.processor.lenient.CommentProcessor;
import dev.tagless.parser.tson.parser.processor.types.PropertyProcessor;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;

import java.nio.CharBuffer;

public class ObjectProcessor extends JsonProcessor {

    private JsonToken expectedToken = JsonToken.STRING_BLOCK;

    private boolean hasProperty = false;
    private boolean expectProperty = false;
    private boolean start = false;
    private boolean completed = false;

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     */
    public ObjectProcessor(final Contextualizer context, final boolean lenient) {
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
                    Token result = this.delegator.getToken();
                    this.result.extend(result);
                    this.hasProperty = true;
                    this.expectProperty = false;

                    this.expectedToken = JsonToken.PROPERTY_SEPARATOR;
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
            if (this.completed) {
                if (token.equals(JsonToken.PROPERTY_SEPARATOR) && lenient) {
                    continue;
                }

                throw new JsonSyntaxException(this.context, character);
            }

            if (!start && !token.equals(JsonToken.BLOCK_OPEN)) {
                throw new JsonTokenException(this.context, character, JsonToken.BLOCK_OPEN);
            }

            if (!start && token.equals(JsonToken.BLOCK_OPEN)) {
                this.start = true;
                this.result = new Token(token);
                continue;
            }

            assert this.result != null;
            if (token.equals(JsonToken.BLOCK_CLOSE)) {
                if (expectProperty && !lenient) {
                    throw new JsonTokenException(this.context, character, JsonToken.STRING_BLOCK);
                }

                this.result.append(token);
                this.completed = true;

                return true;
            }

            if (!token.equals(this.expectedToken)) {
                throw new JsonTokenException(this.context, character, this.expectedToken);
            }

            if (this.expectedToken.equals(JsonToken.STRING_BLOCK)) {
                buffer.position(buffer.position() - 1);
                this.delegator = new PropertyProcessor(this.context, this.lenient);

                if (this.delegator.process(buffer)) {
                    Token result = this.delegator.getToken();
                    this.result.extend(result);
                    this.delegator = null;
                    this.hasProperty = true;
                    this.expectProperty = false;

                    this.expectedToken = JsonToken.PROPERTY_SEPARATOR;
                    continue;
                }
            }

            if (this.expectedToken.equals(JsonToken.PROPERTY_SEPARATOR)) {
                if (!this.hasProperty && !lenient) {
                    throw new JsonTokenException(this.context, character, JsonToken.STRING_BLOCK);
                }

                this.expectedToken = JsonToken.STRING_BLOCK;
                expectProperty = true;
            }

            this.result.append(token);
        }

        return this.completed;
    }
}
