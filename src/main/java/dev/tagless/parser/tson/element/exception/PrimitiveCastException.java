package dev.tagless.parser.tson.element.exception;

import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown when a JSON primitive element
 * tries to access a value as a type which does not correspond
 * to the variable storing the value type
 */
public class PrimitiveCastException extends RuntimeException {

    /**
     * Initialize the exception
     * @param variable the variable
     * @param type the expected variable type
     */
    public PrimitiveCastException(final Object variable, final @NotNull String type) {
        super(String.format("Cannot cast variable of type %s to %s", determineVarType(variable), type));
    }

    private static String determineVarType(final Object variable) {
        return switch (variable) {
            case null -> "null";
            case CharSequence _ -> "string";
            case Boolean _ -> "boolean";
            case Number _ -> "number";
            default -> variable.getClass().getSimpleName().toLowerCase();
        };
    }
}
