package dev.tagless.parser.tson.serializer.writer;

import dev.tagless.parser.tson.hydrator.HydratorChain;
import dev.tagless.parser.tson.parser.token.JsonToken;
import dev.tagless.parser.tson.serializer.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * JsonObject writer
 */
public final class ObjectWriter extends JsonWriter {

    private boolean indentStart;

    /**
     * Create a new JSON writer
     *
     * @param chain the token chain
     */
    public ObjectWriter(final @NotNull HydratorChain chain) {
        super(chain);
    }

    /**
     * Mark the object to be fully indented
     * @return the JSON writer
     */
    public ObjectWriter indent() {
        this.indentStart = true;
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

        final Optional<Character> lineTerminator = Optional.ofNullable(
                increaseBy > 0 ? '\n' : null
        );

        final Consumer<Character> CHAR_LB = character -> {
            try {
                writer.write(character);
            } catch (IOException ignored) {}
        };

        boolean start = false;
        while (this.chain.hasNext()) {
            JsonToken token = this.chain.next();
            if (token.equals(JsonToken.PROPERTY_SEPARATOR)) {
                writer.write(token.getValue());
                lineTerminator.ifPresent(CHAR_LB);
                continue;
            }

            if (token.equals(JsonToken.BLOCK_OPEN)) {
                if (!start) {
                    if (this.indentStart) {
                        String indent = indentMode.get(currentLevel);
                        writer.write(indent);
                    }

                    start = true;
                    writer.write(token.getValue());
                    lineTerminator.ifPresent(CHAR_LB);

                    continue;
                }

                this.chain.previous();
                ObjectWriter sub = new ObjectWriter(this.chain);
                sub.indent(increaseBy)
                        .indentWith(indentMode)
                        .spaceWith(spaceOption)
                        .level(currentLevel + increaseBy)
                        .write(writer);
                lineTerminator.ifPresent(CHAR_LB);
            } else if (token.equals(JsonToken.ARRAY_OPEN)) {
                this.chain.previous();
                ArrayWriter sub = new ArrayWriter(this.chain);
                sub.indent(increaseBy)
                        .indentWith(indentMode)
                        .spaceWith(spaceOption)
                        .level(currentLevel + increaseBy)
                        .write(writer);

                lineTerminator.ifPresent(CHAR_LB);
            } else if (token.equals(JsonToken.BLOCK_CLOSE)) {
                lineTerminator.ifPresent(CHAR_LB);

                String indent = indentMode.get(currentLevel);
                writer.write(indent);
                writer.write(token.getValue());
                break;
            } else {
                this.chain.previous();
                PrimitiveWriter sub = new PrimitiveWriter(this.chain);
                sub.indent(increaseBy)
                        .indentWith(indentMode)
                        .spaceWith(spaceOption)
                        .level(currentLevel + increaseBy)
                        .write(writer);
            }
        }

        writer.flush();
    }
}
