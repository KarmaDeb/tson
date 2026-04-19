package dev.tagless.parser.tson.serializer.writer;

import dev.tagless.parser.tson.hydrator.HydratorChain;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.serializer.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

/**
 * JsonPrimitive writer
 */
public final class PrimitiveWriter extends JsonWriter {

    private boolean skipKey = false;

    /**
     * Create a new JSON writer
     *
     * @param chain the token chain
     */
    public PrimitiveWriter(final @NotNull HydratorChain chain) {
        super(chain);
    }

    /**
     * Skip the key processing. This is used only
     * by {@link ArrayWriter}
     * @return the JSON writer
     */
    @NotNull
    public PrimitiveWriter skipKey() {
        this.skipKey = true;
        return this;
    }

    /**
     * Write the token into the specified writer
     *
     * @param writer the writer
     * @throws IOException if there's a problem while writing
     */
    @Override
    public void write(final @NotNull Writer writer) throws IOException {
        final int currentLevel = this.startLevel;
        final int increaseBy = this.indent;
        final IndentMode indentMode = this.indentMode;
        final SpaceOption spaceOption = this.spaceOption;

        String indent = indentMode.get(this.startLevel);
        writer.write(indent);

        if (!this.skipKey) {
            writer.write(this.chain.next().getValue()); // "
            writer.write(this.chain.next().getValue()); // key
            writer.write(this.chain.next().getValue()); // "

            if (spaceOption.equals(SpaceOption.BEFORE) || spaceOption.equals(SpaceOption.BETWEEN)) {
                writer.write(' ');
            }

            writer.write(this.chain.next().getValue()); // :

            if (spaceOption.equals(SpaceOption.AFTER) || spaceOption.equals(SpaceOption.BETWEEN)) {
                writer.write(' ');
            }
        }

        JsonToken valueToken = this.chain.next();
        if (!valueToken.isValue()) {
            JsonWriter sub = null;

            if (valueToken.equals(JsonToken.BLOCK_OPEN)) {
                this.chain.previous();
                sub = new ObjectWriter(this.chain)
                        .indent(increaseBy)
                        .indentWith(indentMode)
                        .spaceWith(spaceOption)
                        .level(currentLevel);

            } else if (valueToken.equals(JsonToken.ARRAY_OPEN)) {
                this.chain.previous();
                sub = new ArrayWriter(this.chain)
                        .indent(increaseBy)
                        .indentWith(indentMode)
                        .spaceWith(spaceOption)
                        .level(currentLevel);

            } else {
                writer.write(valueToken.getValue()); // "
                writer.write(this.chain.next().getValue()); // value
                writer.write(this.chain.next().getValue()); // "
            }

            if (sub != null) {
                sub.write(writer);
            }
        } else {
            writer.write(valueToken.getValue());
        }

        writer.flush();
    }
}
