package bloomd.decoders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import bloomd.args.StateArgs;
import bloomd.replies.StateResult;

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
    public Optional<T> decode(String msg) throws Exception {
        Optional<List<StateResult>> checkResults = parseStateResult(msg);
        if (!checkResults.isPresent()) {
            return Optional.empty();
        } else {
            List<StateResult> resultList = checkResults.get();
            if (singleItem) {
                T result = (T) resultList.get(0);
                return Optional.of(result);
            } else {
                T result = (T) resultList;
                return Optional.of(result);
            }
        }
    }

    private Optional<List<StateResult>> parseStateResult(String msg) {
        switch (msg) {
            case "Filter does not exist":
                if (singleItem) {
                    return Optional.of(Collections.singletonList(StateResult.DOES_NOT_EXISTS));
                } else {
                    return Optional.of(Collections.emptyList());
                }
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

                return Optional.of(result);
        }
    }
}
