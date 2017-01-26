package bloomd.decoders;

/**
 * @param <ARG>
 * @param <OUTPUT>
 */
public interface BloomdCommandCodec<ARG, OUTPUT> {
    String buildCommand(ARG args);

    OUTPUT decode(String msg) throws Exception;
}