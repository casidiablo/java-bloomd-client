package bloomd.decoders;

/**
 * Single arg commands codec. Used to implement `close`, `drop` and `flush`.
 */
public class SingleArgCodec implements BloomdCommandCodec<String, Boolean> {

    private final String command;

    public SingleArgCodec(String command) {
        this.command = command;
    }

    @Override
    public String buildCommand(String filterName) {
        return command + " " + filterName;
    }

    @Override
    public Boolean decode(String msg) throws Exception {
        switch (msg) {
            case "Done":
                return true;

            case "Filter does not exist":
                return false;

            default:
                throw new RuntimeException(msg);
        }
    }
}
