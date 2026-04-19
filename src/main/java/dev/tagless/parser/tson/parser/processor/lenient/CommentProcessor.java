package dev.tagless.parser.tson.parser.processor.lenient;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.exception.JsonException;

import java.nio.CharBuffer;

public class CommentProcessor extends JsonProcessor {

    private char[] expectedChar = {};
    private boolean start = false;
    private boolean bash = false;

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     */
    public CommentProcessor(final Contextualizer context, final boolean lenient) {
        super(context, lenient);
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
        if (this.completed) {
            return true;
        }

        while (buffer.hasRemaining()) {
            char character = buffer.get();
            this.context.column += 1;
            if (character == '\n') {
                this.context.lb();
            }

            if (!start) {
                if (character == '/' || character == '#') {
                    start = true;
                    bash = character == '#';
                }
                continue;
            }

            if (expectedChar.length == 0) {
                if (character == '/' || bash) {
                    expectedChar = new char[]{'\n'};
                } else if (character == '*') {
                    expectedChar = new char[]{'*', '/'};
                }
            }

            if (character == expectedChar[0]) {
                char[] n = new char[expectedChar.length - 1];
                if (n.length == 0) {
                    this.completed = true;
                    return true;
                }

                System.arraycopy(expectedChar, 1, n, 0, n.length);
                this.expectedChar = n;
            } else {
                if (expectedChar[0] != '\n' && expectedChar[0] != '*') {
                    expectedChar = new char[]{'*', '/'};
                }
            }
        }

        return this.completed;
    }
}
