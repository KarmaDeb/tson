package dev.tagless.parser.tson.parser.lexer.extensions.scheme;

import dev.tagless.parser.tson.parser.lexer.extensions.ExtensionStream;
import dev.tagless.parser.tson.parser.lexer.extensions.JsonLexerExtension;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * jar scheme extension
 */
public final class JarExtension extends JsonLexerExtension {

    public static final JsonLexerExtension INSTANCE = new JarExtension();

    /**
     * Create a new JSON lexer extension
     */
    JarExtension() {
        super(new String[]{"jar"});
    }

    /**
     * Process a URI
     *
     * @param uri the uri
     * @return the processed URI
     * @throws Throwable if there's any problem while processing
     *                   the URI
     */
    @Override
    public @NotNull ExtensionStream process(final @NotNull URI uri) throws Throwable {
        String path = uri.getPath();
        String jarLocation = path.split("!/")[0];
        String fileLocation = path.substring(jarLocation.length() + 2);

        JarFile jar = new JarFile(jarLocation, true);

        ZipEntry entry = jar.getEntry(fileLocation);
        if (entry == null) {
            jar.close();
            throw new IOException(String.format("Could not find %s on %s from uri: %s",
                    fileLocation, jarLocation, uri));
        }

        return new ExtensionStream(jar.getInputStream(entry), jar);
    }
}
