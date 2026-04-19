package dev.tagless.parser.tson.serializer;

import dev.tagless.parser.tson.element.JsonElement;
import dev.tagless.parser.tson.hydrator.HydratorChain;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.parser.token.Token;
import dev.tagless.parser.tson.serializer.writer.ArrayWriter;
import dev.tagless.parser.tson.serializer.writer.ObjectWriter;
import dev.tagless.parser.tson.serializer.writer.PrimitiveWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * JSON writer from a JSON token
 */
public abstract class JsonWriter {

    protected final HydratorChain chain;

    protected IndentMode indentMode = IndentMode.SPACE;
    protected SpaceOption spaceOption = SpaceOption.NONE;
    protected int indent = 0;
    protected int startLevel = 0;

    /**
     * Create a new JSON writer
     * @param chain the token chain
     */
    protected JsonWriter(final @NotNull HydratorChain chain) {
        this.chain = chain;
    }

    /**
     * Set the indent level. Setting an indent
     * level of zero disables indentation
     * @param indent the indent level
     * @return the JSON writer
     */
    @NotNull
    public final JsonWriter indent(final int indent) {
        this.indent = Math.max(0, indent);
        return this;
    }

    /**
     * Set the "start" level for the
     * writer
     * @param level the current level
     * @return the JSON writer
     */
    @NotNull
    public final JsonWriter level(final int level) {
        this.startLevel = level;
        return this;
    }

    /**
     * Set the indent mode
     * @param mode the mode
     * @return the JSON writer
     */
    @NotNull
    public final JsonWriter indentWith(final @NotNull IndentMode mode) {
        this.indentMode = mode;
        return this;
    }

    /**
     * Set the space mode
     * @param space the space
     * @return the JSON writer
     */
    @NotNull
    public final JsonWriter spaceWith(final @NotNull SpaceOption space) {
        this.spaceOption = space;
        return this;
    }

    /**
     * Write the token into a buffered string
     * @return the buffered string
     * @throws IOException if there's a problem while writing
     */
    @NotNull
    public String write() throws IOException {
        try (StringWriter writer = new StringWriter()) {
            this.write(writer);
            return writer.toString();
        }
    }

    /**
     * Write the token into the specified writer
     * @param writer the writer
     * @throws IOException if there's a problem while writing
     */
    public abstract void write(final @NotNull Writer writer) throws IOException;

    /**
     * Create a new JSON writer
     * @param element the element to write
     * @return the JSON writer
     */
    @NotNull
    public static <T extends JsonElement> JsonWriter writer(final @NotNull T element) {
        JsonTokenizer<T> tokenizer = JsonTokenizer.create(element);
        Token token = tokenizer.process().first();
        token.clean();

        HydratorChain chain = new HydratorChain(token);
        return new DefaultJsonWriter(chain);
    }

    /**
     * Indentation modes
     */
    public enum IndentMode {
        /**
         * Simple space
         */
        SPACE(' '),
        /**
         * Tab character
         */
        TAB('\t');

        private final char character;

        /**
         * Create a new indent mode
         * @param character the indent character
         */
        IndentMode(final char character) {
            this.character = character;
        }

        /**
         * Get the indent
         * @param level the indent level
         * @return the indent
         */
        @NotNull
        public String get(final int level) {
            if (level <= 0) {
                return "";
            }

            return new StringBuilder().repeat(this.character, level)
                    .toString();
        }
    }

    public enum SpaceOption {
        /**
         * Insert no spaces
         */
        NONE,
        /**
         * After the ":" => "key: value"
         */
        AFTER,
        /**
         * Before the ":" => "key :value"
         */
        BEFORE,
        /**
         * Between the ":" => "key : value"
         */
        BETWEEN
    }
}

final class DefaultJsonWriter extends JsonWriter {

    /**
     * Create a new JSON writer
     *
     * @param chain the token chain
     */
    DefaultJsonWriter(final @NotNull HydratorChain chain) {
        super(chain);
    }

    /**
     * Write the token into the specified writer
     *
     * @param writer the writer
     * @throws IOException if there's a problem while writing
     */
    @Override
    public void write(final @NotNull Writer writer) throws IOException {
        int increaseBy = this.indent;
        IndentMode indentMode = this.indentMode;
        SpaceOption spaceOption = this.spaceOption;

        while (this.chain.hasNext()) {
            JsonToken token = this.chain.next();

            JsonWriter subWriter;
            if (token.equals(JsonToken.BLOCK_OPEN)) {
                subWriter = new ObjectWriter(this.chain);
            } else if (token.equals(JsonToken.ARRAY_OPEN)) {
                subWriter = new ArrayWriter(this.chain);
            } else {
                subWriter = new PrimitiveWriter(this.chain);
            }

            this.chain.previous();
            subWriter.indent(increaseBy)
                    .indentWith(indentMode)
                    .spaceWith(spaceOption)
                    .write(writer);
        }

        writer.flush();
    }
}