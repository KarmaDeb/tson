package dev.tagless.parser.tson.parser.processor.types.property;

import dev.tagless.parser.tson.parser.JsonProcessor;
import dev.tagless.parser.tson.parser.exception.JsonException;
import dev.tagless.parser.tson.parser.exception.JsonSyntaxException;
import dev.tagless.parser.tson.parser.exception.JsonTokenException;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;

import java.nio.CharBuffer;

public class AnyProcessor extends JsonProcessor {

    private boolean isBooleanTrue = false;
    private boolean isBooleanFalse = false;
    private boolean isNumber = false;
    private boolean isNull = false;
    private int nullConsumptions = 2;
    private boolean numberDecimalPart = true;
    private boolean numberExponential = true;

    private char nextChar = '\0';

    private final StringBuilder numberBuilder = new StringBuilder();

    /**
     * Creates a new JSON processor
     *
     * @param context the current parser context
     * @param lenient if the processor is in lenient
     *                mode
     */
    public AnyProcessor(final Contextualizer context, final boolean lenient) {
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
        if (this.checkBuffer(buffer)) {
            return true;
        }

        while (buffer.hasRemaining()) {
            this.context.column += 1;
            char character = buffer.get();

            if (!isBooleanTrue && !isBooleanFalse && !isNumber && !isNull) {
                if (character == 't') {
                    this.isBooleanTrue = true;
                    this.nextChar = 'r';
                    continue;
                } else if (character == 'f') {
                    this.isBooleanFalse = true;
                    this.nextChar = 'a';
                    continue;
                } else if (character == 'n') {
                    this.isNull = true;
                    this.nextChar = 'u';
                    continue;
                } else if (character == '+' || character == '-' || Character.isDigit(character) || character == '.' ||
                        character == 'e') {
                    if (character == '.') {
                        this.numberDecimalPart = false;
                    }
                    if (character == 'e') {
                        this.numberExponential = false;
                    }

                    this.isNumber = true;
                    this.numberBuilder.append(character);
                    continue;
                }

                throw new JsonSyntaxException(this.context, character);
            }

            if (nextChar != '\0') {
                if (character != this.nextChar) {
                    throw new JsonTokenException(this.context, character, new JsonToken(String.valueOf(this.nextChar)));
                }

                if (this.isBooleanTrue) {
                    switch (character) {
                        case 'r':
                            this.nextChar = 'u';
                            break;
                        case 'u':
                            this.nextChar = 'e';
                            break;
                        case 'e':
                            this.result = new Token(JsonToken.KEYWORD_TRUE);
                            this.completed = true;
                            return true;
                    }
                } else if (this.isBooleanFalse) {
                    switch (character) {
                        case 'a':
                            this.nextChar = 'l';
                            break;
                        case 'l':
                            this.nextChar = 's';
                            break;
                        case 's':
                            this.nextChar = 'e';
                            break;
                        case 'e':
                            this.result = new Token(JsonToken.KEYWORD_FALSE);
                            this.completed = true;
                            return true;
                    }
                } else if (this.isNull) {
                    this.nextChar = 'l';
                    if (this.nullConsumptions-- <= 0) {
                        this.result = new Token(JsonToken.KEYWORD_NULL);
                        this.completed = true;
                        return true;
                    }
                }
            } else {
                if (character == '.') {
                    if (!this.numberDecimalPart) {
                        if (this.lenient) {
                            continue;
                        }

                        throw new JsonSyntaxException(this.context, character);
                    }

                    this.numberBuilder.append('.');
                    this.numberDecimalPart = false;
                } else if (character == 'e') {
                    if (!this.numberExponential) {
                        if (this.lenient) {
                            continue;
                        }

                        throw new JsonSyntaxException(this.context, character);
                    }

                    this.numberBuilder.append('e');
                    this.numberExponential = false;
                } else if (!Character.isDigit(character)) {
                    buffer.position(buffer.position() - 1);
                    this.result = new Token(new JsonToken(this.numberBuilder.toString()).asNumber());

                    this.completed = true;
                    return true;
                }

                this.numberBuilder.append(character);
            }
        }

        return this.completed;
    }
}
