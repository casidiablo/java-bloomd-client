package bloomd.decoders;

import java.util.Optional;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.CreateResult;

public class CreateCodec implements BloomdCommandCodec<CreateFilterArgs, CreateResult> {

    @Override
    public String buildCommand(CreateFilterArgs args) {
        StringBuilder command = new StringBuilder();

        command.append("create ");
        command.append(args.getFilterName());

        if (args.getCapacity() != null) {
            command.append(" capacity=");
            command.append(args.getCapacity());
        }

        if (args.getFalsePositiveProbability() != null) {
            command.append(" prob=");
            command.append(String.format("%f", args.getFalsePositiveProbability()));
        }

        if (args.getInMemory() != null) {
            command.append(" in_memory=");
            command.append(args.getInMemory() ? 1 : 0);
        }

        return command.toString();
    }

    @Override
    public Optional<CreateResult> decode(String msg) throws Exception {
        switch (msg) {
            case "Done":
                return Optional.of(CreateResult.DONE);

            case "Exists":
                return Optional.of(CreateResult.EXISTS);

            case "Delete in progress":
                return Optional.of(CreateResult.DELETE_IN_PROGRESS);

            default:
                throw new RuntimeException(msg);
        }
    }
}
