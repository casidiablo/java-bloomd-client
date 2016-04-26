package bloomd.decoders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import bloomd.replies.BloomdFilter;

public class ListCodec implements BloomdCommandCodec<String, List<BloomdFilter>> {

    private final List<BloomdFilter> filters = new ArrayList<>();

    @Override
    public String buildCommand(String args) {
        if (args.isEmpty()) {
            return "list";
        } else {
            return "list " + args;
        }
    }

    @Override
    public Optional<List<BloomdFilter>> decode(String msg) throws Exception {
        switch (msg) {
            case "START":
                if (!filters.isEmpty()) {
                    filters.clear();
                    throw new IllegalStateException("START not expected. List already initialized.");
                }

                return Optional.empty();

            case "END":
                List<BloomdFilter> results = Collections.unmodifiableList(new ArrayList<>(filters));

                filters.clear();// TODO explain

                return Optional.of(results);

            default:
                String[] parts = msg.split(" ");

                String filterName = parts[0];
                float falsePositiveProbability = Float.parseFloat(parts[1]);
                long sizeBytes = Long.parseLong(parts[2]);
                long capacity = Long.parseLong(parts[3]);
                long size = Long.parseLong(parts[4]);

                filters.add(new BloomdFilter(filterName, falsePositiveProbability, sizeBytes, capacity, size));

                return Optional.empty();
        }
    }
}
