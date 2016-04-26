package bloomd.replies;

public class BloomdInfo {
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

    public BloomdInfo(long capacity, long checks, long checkHits, long checkMisses, boolean inMemory, long pageIns, long pageOuts, float probability, long sets, long setHits, long setMisses, long size, long storage) {
        this.capacity = capacity;
        this.checks = checks;
        this.checkHits = checkHits;
        this.checkMisses = checkMisses;
        this.inMemory = inMemory;
        this.pageIns = pageIns;
        this.pageOuts = pageOuts;
        this.probability = probability;
        this.sets = sets;
        this.setHits = setHits;
        this.setMisses = setMisses;
        this.size = size;
        this.storage = storage;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getChecks() {
        return checks;
    }

    public long getCheckHits() {
        return checkHits;
    }

    public long getCheckMisses() {
        return checkMisses;
    }

    public boolean isInMemory() {
        return inMemory;
    }

    public long getPageIns() {
        return pageIns;
    }

    public long getPageOuts() {
        return pageOuts;
    }

    public float getProbability() {
        return probability;
    }

    public long getSets() {
        return sets;
    }

    public long getSetHits() {
        return setHits;
    }

    public long getSetMisses() {
        return setMisses;
    }

    public long getSize() {
        return size;
    }

    public long getStorage() {
        return storage;
    }
}
