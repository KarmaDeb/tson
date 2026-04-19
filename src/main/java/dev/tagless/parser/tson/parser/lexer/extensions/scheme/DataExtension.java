package dev.tagless.parser.tson.parser.lexer.extensions.scheme;

import dev.tagless.parser.tson.FunctionChain;
import dev.tagless.parser.tson.parser.lexer.extensions.ExtensionStream;
import dev.tagless.parser.tson.parser.lexer.extensions.JsonLexerExtension;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.HexFormat;

/**
 * data scheme extension
 */
public final class DataExtension extends JsonLexerExtension {

    public static final JsonLexerExtension INSTANCE = new DataExtension();

    /**
     * Create a new JSON lexer extension
     */
    DataExtension() {
        super(new String[]{"data"});
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
        String path = uri.getSchemeSpecificPart();
        String data = path.split(",")[0];
        String contentType = path.split(";")[0];
        String encoding = path.split(";")[1];
        String encoded = path.substring(data.length() + 1);

        if (!contentType.equals("application/json") &&
                !contentType.equals("application/problem+json") &&
                !contentType.equals("text/plain")) {
            throw new IOException("Cannot decode non-json data URI");
        }

        byte[] decoded = switch (encoding.toLowerCase()) {
            case "base64", "hex" -> ((FunctionChain<String, byte[]>) Base64.getDecoder()::decode)
                    .except(IllegalArgumentException.class, Base64.getUrlDecoder()::decode)
                    .except(IllegalArgumentException.class, Base64.getMimeDecoder()::decode)
                    .except(IllegalArgumentException.class, HexFormat.of()::parseHex)
                    .apply(encoded);
            case null, default -> throw new IOException("Unknown data uri encoding: " + encoding);
        };

        return new ExtensionStream(new ByteArrayInputStream(decoded));
    }
}
