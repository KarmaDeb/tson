package dev.tagless.parser.tson.parser;

import dev.tagless.parser.tson.parser.exception.JsonEOFException;
import dev.tagless.parser.tson.parser.exception.JsonException;
import dev.tagless.parser.tson.parser.exception.JsonSyntaxException;
import dev.tagless.parser.tson.parser.exception.JsonTokenException;
import dev.tagless.parser.tson.parser.processor.ArrayProcessor;
import dev.tagless.parser.tson.parser.processor.ObjectProcessor;
import dev.tagless.parser.tson.parser.processor.lenient.CommentProcessor;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;
import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;

/**
 * A processor is a context-aware processor which allows
 * to correctly build a series of {@link Token}
 */
public abstract class JsonProcessor {

    protected final Contextualizer context;
    protected final boolean lenient;

    protected JsonProcessor delegator;
    protected Token result = new Token(JsonToken.UNKNOWN);
    protected boolean completed;

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     */
    public JsonProcessor(final Contextualizer context, final boolean lenient) {
        this.lenient = lenient;
        this.context = context;
    }

    /**
     * Process the buffer
     * @param buffer the buffer to process
     * @return if the processing has finished
     * @throws JsonException if there's a problem the processor
     * can not handle
     */
    public abstract boolean process(final CharBuffer buffer) throws JsonException;

    /**
     * Get the processed token
     * @return the processed token
     */
    @NotNull
    public Token getToken() {
        return this.result;
    }

    /**
     * Check the buffer length and the processor
     * status
     * @param buffer the buffer to check
     * @return if the check failed
     * @throws JsonException if the buffer cannot be checked
     */
    protected final boolean checkBuffer(final CharBuffer buffer) throws JsonException {
        if (this.completed) {
            if (!buffer.hasRemaining() || this.lenient) {
                return true;
            }

            throw new JsonEOFException(this.context, buffer.get());
        }

        return false;
    }

    /**
     * Create a normal JSON processor
     * @return the JSON processor
     */
    public static JsonProcessor create() {
        return new RootProcessor(false);
    }

    /**
     * Create a new lenient processor
     * @return the JSON lenient processor
     */
    public static JsonProcessor createLenient() {
        return new RootProcessor(true);
    }

    public static class Contextualizer {

        public int line = 0;
        public int column = 1;

        public void lb() {
            this.line += 1;
            this.column = 1;
        }
    }
}

class RootProcessor extends JsonProcessor {

    private JsonProcessor delegator;

    /**
     * Creates a new JSON processor
     *
     * @param lenient if the processor is in lenient
     *                mode
     */
    public RootProcessor(final boolean lenient) {
        super(new Contextualizer(), lenient);
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
                    this.result = this.delegator.getToken();
                    return true;
                }
            } else {
                return false;
            }
        }

        JsonToken token = null;
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

            token = JsonToken.getToken(character, this.lenient);
            if (!token.equals(JsonToken.BLOCK_OPEN) && !token.equals(JsonToken.ARRAY_OPEN)) {
                if (this.lenient) {
                    continue;
                }

                throw new JsonTokenException(this.context, character, JsonToken.BLOCK_OPEN);
            }

            break;
        }

        if (token == null) {
            throw new JsonTokenException(this.context, '\0', JsonToken.BLOCK_OPEN);
        }

        if (token.equals(JsonToken.BLOCK_OPEN)) {
            buffer.position(buffer.position() - 1);

            this.delegator = new ObjectProcessor(this.context, this.lenient);
            return this.process(buffer);
        } else if (token.equals(JsonToken.ARRAY_OPEN)) {
            buffer.position(buffer.position() - 1);

            this.delegator = new ArrayProcessor(this.context, this.lenient);
            return this.process(buffer);
        }

        throw new JsonTokenException(this.context, token.getValue().charAt(0), JsonToken.BLOCK_OPEN);
    }
}