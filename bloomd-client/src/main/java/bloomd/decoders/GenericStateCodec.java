package bloomd.decoders;

import bloomd.FilterDoesNotExistException;
import bloomd.args.StateArgs;
import bloomd.replies.StateResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic codec implementation for the SET, CHECK, MULTI and BULK commands.
 *
 * They share the same syntax: command filter_name key [key2 [keyN]]
 */
public class GenericStateCodec<T> implements BloomdCommandCodec<StateArgs, T> {

    private final String cmd;
    private final boolean singleItem;

    public GenericStateCodec(String cmd, boolean singleItem) {
        this.cmd = cmd;
        this.singleItem = singleItem;
    }

    @Override
    public String buildCommand(StateArgs args) {
        StringBuilder command = new StringBuilder();
        command.append(cmd);
        command.append(" ");

        command.append(args.getFilterName());

        for (String key : args.getKeys()) {
            command.append(" ");
            command.append(key);
        }

        return command.toString();
    }

    @Override
    public T decode(String msg) throws Exception {
        List<StateResult> checkResults = parseStateResult(msg);
        if (singleItem) {
            T result = (T) checkResults.get(0);
            return result;
        } else {
            T result = (T) checkResults;
            return result;
        }
    }

    private List<StateResult> parseStateResult(String msg) {
        switch (msg) {
            case "Filter does not exist":
                throw new FilterDoesNotExistException(msg);

            default:
                String[] parts = msg.split(" ");

                List<StateResult> result = new ArrayList<>();

                for (String part : parts) {
                    switch (part) {
                        case "Yes":
                            result.add(StateResult.YES);
                            break;
                        case "No":
                            result.add(StateResult.NO);
                            break;
                        default:
                            throw new IllegalStateException("Invalid result: " + msg);
                    }
                }

                return result;
        }
    }
}
