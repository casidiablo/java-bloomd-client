package bloomd.decoders;

import bloomd.FilterDoesNotExistException;
import bloomd.replies.BloomdInfo;

public class InfoCodec implements BloomdCommandCodec<String, BloomdInfo> {

    private BloomdInfoBuilder builder = null;

    @Override
    public String buildCommand(String filterName) {
        return "info " + filterName;
    }

    @Override
    public BloomdInfo decode(String msg) throws Exception {
        switch (msg) {
            case "Filter does not exist":
                throw new FilterDoesNotExistException(msg);

            case "START":
                if (builder != null) {
                    throw new IllegalStateException("START not expected. Builder already initialized.");
                }

                builder = new BloomdInfoBuilder();

                return null;

            case "END":
                if (builder == null) {
                    throw new IllegalStateException("END not expected. Builder was not initialized.");
                }

                BloomdInfo bloomdInfo = builder.createBloomdInfo();

                builder = null;

                return bloomdInfo;

            default:
                String[] parts = msg.split(" ");

                if (parts.length != 2) {
                    throw new IllegalStateException("Invalid info line: " + msg);
                }

                String key = parts[0];
                String value = parts[1];

                switch (key) {
                    case "capacity":
                        builder.setCapacity(Long.parseLong(value));
                        break;
                    case "checks":
                        builder.setChecks(Long.parseLong(value));
                        break;
                    case "check_hits":
                        builder.setCheckHits(Long.parseLong(value));
                        break;
                    case "check_misses":
                        builder.setCheckMisses(Long.parseLong(value));
                        break;
                    case "in_memory":
                        builder.setInMemory(Integer.parseInt(value) == 1);
                        break;
                    case "page_ins":
                        builder.setPageIns(Long.parseLong(value));
                        break;
                    case "page_outs":
                        builder.setPageOuts(Long.parseLong(value));
                        break;
                    case "probability":
                        builder.setProbability(Float.parseFloat(value));
                        break;
                    case "sets":
                        builder.setSets(Long.parseLong(value));
                        break;
                    case "set_hits":
                        builder.setSetHits(Long.parseLong(value));
                        break;
                    case "set_misses":
                        builder.setSetMisses(Long.parseLong(value));
                        break;
                    case "size":
                        builder.setSize(Long.parseLong(value));
                        break;
                    case "storage":
                        builder.setStorage(Long.parseLong(value));
                        break;
                }

                return null;
        }
    }

    private static class BloomdInfoBuilder {
        private long capacity;
        private long checks;
        private long checkHits;
        private long checkMisses;
        private boolean inMemory;
        private long pageIns;
        private long pageOuts;
        private float probability;
        private long sets;
        private long setHits;
        private long setMisses;
        private long size;
        private long storage;

        public BloomdInfoBuilder setCapacity(long capacity) {
            this.capacity = capacity;
            return this;
        }

        public BloomdInfoBuilder setChecks(long checks) {
            this.checks = checks;
            return this;
        }

        public BloomdInfoBuilder setCheckHits(long checkHits) {
            this.checkHits = checkHits;
            return this;
        }

        public BloomdInfoBuilder setCheckMisses(long checkMisses) {
            this.checkMisses = checkMisses;
            return this;
        }

        public BloomdInfoBuilder setInMemory(boolean inMemory) {
            this.inMemory = inMemory;
            return this;
        }

        public BloomdInfoBuilder setPageIns(long pageIns) {
            this.pageIns = pageIns;
            return this;
        }

        public BloomdInfoBuilder setPageOuts(long pageOuts) {
            this.pageOuts = pageOuts;
            return this;
        }

        public BloomdInfoBuilder setProbability(float probability) {
            this.probability = probability;
            return this;
        }

        public BloomdInfoBuilder setSets(long sets) {
            this.sets = sets;
            return this;
        }

        public BloomdInfoBuilder setSetHits(long setHits) {
            this.setHits = setHits;
            return this;
        }

        public BloomdInfoBuilder setSetMisses(long setMisses) {
            this.setMisses = setMisses;
            return this;
        }

        public BloomdInfoBuilder setSize(long size) {
            this.size = size;
            return this;
        }

        public BloomdInfoBuilder setStorage(long storage) {
            this.storage = storage;
            return this;
        }

        public BloomdInfo createBloomdInfo() {
            return new BloomdInfo(capacity, checks, checkHits, checkMisses,
                                  inMemory, pageIns, pageOuts, probability,
                                  sets, setHits, setMisses, size, storage);
        }
    }
}
