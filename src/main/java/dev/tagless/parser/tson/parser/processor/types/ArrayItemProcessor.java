package dev.tagless.parser.tson.parser.processor.types;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.exception.JsonException;
import dev.tagless.parser.tson.parser.exception.JsonSyntaxException;
import dev.tagless.parser.tson.parser.processor.ArrayProcessor;
import dev.tagless.parser.tson.parser.processor.ObjectProcessor;
import dev.tagless.parser.tson.parser.processor.lenient.CommentProcessor;
import dev.tagless.parser.tson.parser.processor.types.property.AnyProcessor;
import dev.tagless.parser.tson.parser.processor.types.property.StringProcessor;
import dev.tagless.parser.tson.parser.token.JsonToken;

import java.nio.CharBuffer;

public class ArrayItemProcessor extends JsonProcessor {

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     */
    public ArrayItemProcessor(final Contextualizer context, final boolean lenient) {
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
            this.delegatorWork(buffer);
            if (this.completed) {
                return true;
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

            buffer.position(buffer.position() - 1);
            if (token.equals(JsonToken.BLOCK_OPEN)) {
                this.delegator = new ObjectProcessor(this.context, this.lenient);
            } else if (token.equals(JsonToken.ARRAY_OPEN)) {
                this.delegator = new ArrayProcessor(this.context, this.lenient);
            } else if (token.equals(JsonToken.STRING_BLOCK)) {
                this.delegator = new StringProcessor(this.context, this.lenient);
            } else {
                this.delegator = new AnyProcessor(this.context, this.lenient);
            }

            this.delegatorWork(buffer);
            if (this.completed) {
                return true;
            }
        }

        return this.completed;
    }

    private void delegatorWork(final CharBuffer buffer) throws JsonException {
        if (this.delegator.process(buffer)) {
            if (!(this.delegator instanceof CommentProcessor)) {
                this.result.extend(this.delegator.getToken());
                this.completed = true;
            }

            this.delegator = null;
        }
    }
}
