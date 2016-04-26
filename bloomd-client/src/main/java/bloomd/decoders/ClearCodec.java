package bloomd.decoders;

import java.util.Optional;

import bloomd.replies.ClearResult;

public class ClearCodec implements BloomdCommandCodec<String, ClearResult> {

    @Override
    public String buildCommand(String filterName) {
        return "clear " + filterName;
    }

    @Override
    public Optional<ClearResult> decode(String msg) throws Exception {
        switch (msg) {
            case "Done":
                return Optional.of(ClearResult.CLEARED);

            case "Filter does not exist":
                return Optional.of(ClearResult.FILTER_DOES_NOT_EXISTS);

            case "Filter is not proxied. Close it first.":
                return Optional.of(ClearResult.CANNOT_CLEAR);

            default:
                throw new RuntimeException(msg);
        }
    }
}
