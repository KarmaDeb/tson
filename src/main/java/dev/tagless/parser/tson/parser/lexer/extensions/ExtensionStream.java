package dev.tagless.parser.tson.parser.lexer.extensions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * The extension stream is a wrapper for multi-stream data
 * which reads from a single stream
 */
public final class ExtensionStream extends InputStream {

    private final InputStream stream;
    private final Collection<AutoCloseable> pending;

    /**
     * Create a new extension stream
     * @param stream the stream to read from
     * @param pending the streams required for the main stream
     */
    public ExtensionStream(final @NotNull InputStream stream, final AutoCloseable... pending) {
        this.stream = stream;
        this.pending = Arrays.asList(pending);
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an {@code int} in the range {@code 0} to
     * {@code 255}. If no byte is available because the end of the stream
     * has been reached, the value {@code -1} is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return the next byte of data, or {@code -1} if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        return this.stream.read();
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * @throws IOException if an I/O error occurs.
     * @implSpec The {@code close} method of {@code InputStream} does
     * nothing.
     */
    @Override
    public void close() throws IOException {
        for (AutoCloseable closeable : this.pending) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }

        this.pending.clear();
        this.stream.close();
    }
}
