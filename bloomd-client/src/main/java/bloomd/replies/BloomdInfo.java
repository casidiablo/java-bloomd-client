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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BloomdInfo that = (BloomdInfo) o;

        if (capacity != that.capacity) return false;
        if (checks != that.checks) return false;
        if (checkHits != that.checkHits) return false;
        if (checkMisses != that.checkMisses) return false;
        if (inMemory != that.inMemory) return false;
        if (pageIns != that.pageIns) return false;
        if (pageOuts != that.pageOuts) return false;
        if (Float.compare(that.probability, probability) != 0) return false;
        if (sets != that.sets) return false;
        if (setHits != that.setHits) return false;
        if (setMisses != that.setMisses) return false;
        if (size != that.size) return false;
        if (storage != that.storage) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (capacity ^ (capacity >>> 32));
        result = 31 * result + (int) (checks ^ (checks >>> 32));
        result = 31 * result + (int) (checkHits ^ (checkHits >>> 32));
        result = 31 * result + (int) (checkMisses ^ (checkMisses >>> 32));
        result = 31 * result + (inMemory ? 1 : 0);
        result = 31 * result + (int) (pageIns ^ (pageIns >>> 32));
        result = 31 * result + (int) (pageOuts ^ (pageOuts >>> 32));
        result = 31 * result + (probability != +0.0f ? Float.floatToIntBits(probability) : 0);
        result = 31 * result + (int) (sets ^ (sets >>> 32));
        result = 31 * result + (int) (setHits ^ (setHits >>> 32));
        result = 31 * result + (int) (setMisses ^ (setMisses >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (storage ^ (storage >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "BloomdInfo{" +
                "capacity=" + capacity +
                ", checks=" + checks +
                ", checkHits=" + checkHits +
                ", checkMisses=" + checkMisses +
                ", inMemory=" + inMemory +
                ", pageIns=" + pageIns +
                ", pageOuts=" + pageOuts +
                ", probability=" + probability +
                ", sets=" + sets +
                ", setHits=" + setHits +
                ", setMisses=" + setMisses +
                ", size=" + size +
                ", storage=" + storage +
                '}';
    }
}
