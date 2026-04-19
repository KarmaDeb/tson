package dev.tagless.parser.tson.element.navigator.path.processor;

import dev.tagless.parser.tson.element.navigator.path.PathProcessor;
import dev.tagless.parser.tson.element.navigator.path.PathToken;
import dev.tagless.parser.tson.element.navigator.path.exception.BadTokenPosition;
import dev.tagless.parser.tson.element.navigator.path.exception.PathException;
import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;
import java.util.LinkedList;

import static dev.tagless.parser.tson.element.navigator.path.PathToken.*;

/**
 * Word path processor
 */
public final class WordProcessor extends PathProcessor {

    /**
     * Create a new path processor
     *
     * @param buffer the path buffer
     * @param tokens the path tokens
     */
    public WordProcessor(final @NotNull CharBuffer buffer, final LinkedList<PathToken<?>> tokens) {
        super(buffer, tokens);
    }

    /**
     * Process the path
     * @throws PathException if there's a problem while processing
     * the path
     */
    @Override
    public void process() throws PathException {
        StringBuilder currentPart = new StringBuilder();
        boolean escape = false;

        while (buffer.hasRemaining()) {
            char character = buffer.get();
            if (escape) {
                currentPart.append(character);
                escape = false;
            }

            if (character == '\\') {
                escape = true;
                continue;
            }

            if (character == '.' || character == '[') {
                if (!currentPart.isEmpty()) {
                    this.tokens.add(CHARWORD.bind(currentPart.toString()));
                }
                if (buffer.position() == buffer.length() - 1) {
                    currentPart.setLength(0);
                    break;
                }

                if (character == '.') {
                    this.tokens.add(SPACE);
                } else {
                    int before = buffer.position();
                    buffer.position(buffer.position() - 1);
                    try {
                        IndexProcessor processor = new IndexProcessor(this.buffer, this.tokens);
                        processor.process();
                    } catch (BadTokenPosition ignored) {
                        buffer.position(before);

                        this.tokens.removeLast();
                        currentPart.append(character);
                        continue;
                    }
                }

                currentPart.setLength(0);
                continue;
            }

            currentPart.append(character);
        }

        if (!currentPart.isEmpty()) {
            this.tokens.add(CHARWORD.bind(currentPart.toString()));
        }
    }
}
