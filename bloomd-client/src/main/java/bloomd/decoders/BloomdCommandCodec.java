package bloomd.decoders;

import java.util.Optional;

/**
 * TODO Explain
 * @param <ARG>
 * @param <OUTPUT>
 */
public interface BloomdCommandCodec<ARG, OUTPUT> {
    String buildCommand(ARG args);

    Optional<OUTPUT> decode(String msg) throws Exception;
}