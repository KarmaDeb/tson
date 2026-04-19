package dev.tagless.parser.tson;

/**
 * A function chain allows to chain multiple functions
 * into a single call. This is useful for try-chain in where
 * if operation A fails, operation B should be tried
 * @param <P> the parameter the function accepts
 * @param <V> the value the function returns
 */
@FunctionalInterface
public interface FunctionChain<P, V> {

    /**
     * Execute the function
     * @param param the function parameter
     * @return the value
     */
    V apply(final P param);

    /**
     * Add an except clause
     * @param type the exception type
     * @param other the next function
     * @return the chained function
     */
    default FunctionChain<P, V> except(final Class<? extends Throwable> type,
                                       final FunctionChain<P, V> other) {
        return v -> {
            try {
                return this.apply(v);
            } catch (Throwable ex) {
                if (type.isInstance(ex)) {
                    return other.apply(v);
                }

                throw ex;
            }
        };
    }
}
